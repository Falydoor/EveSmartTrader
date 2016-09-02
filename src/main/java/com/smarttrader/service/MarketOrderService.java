package com.smarttrader.service;

import com.beimin.eveapi.exception.ApiException;
import com.beimin.eveapi.parser.ApiAuthorization;
import com.beimin.eveapi.parser.pilot.MarketOrdersParser;
import com.beimin.eveapi.response.shared.MarketOrdersResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.smarttrader.domain.InvType;
import com.smarttrader.domain.MarketOrder;
import com.smarttrader.domain.SellableInvType;
import com.smarttrader.domain.User;
import com.smarttrader.domain.enums.Region;
import com.smarttrader.domain.enums.SellableInvMarketGroup;
import com.smarttrader.domain.enums.Station;
import com.smarttrader.domain.util.CrestBuilder;
import com.smarttrader.domain.util.GsonBean;
import com.smarttrader.repository.InvTypeRepository;
import com.smarttrader.repository.MarketOrderRepository;
import com.smarttrader.repository.SellableInvTypeRepository;
import com.smarttrader.repository.search.MarketOrderSearchRepository;
import com.smarttrader.service.dto.TradeDTO;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Service class for managing market orders.
 */
@Service
@Transactional
public class MarketOrderService {

    private final Logger log = LoggerFactory.getLogger(MarketOrderService.class);

    @Inject
    private MarketOrderRepository marketOrderRepository;

    @Inject
    private MarketOrderSearchRepository marketOrderSearchRepository;

    @Inject
    private SellableInvTypeRepository sellableInvTypeRepository;

    @Inject
    private InvTypeRepository invTypeRepository;

    @Inject
    private UserService userService;

    @Inject
    private GsonBean gsonBean;

    private Map<Long, SellableInvType> sellableByTypeId;

    private List<MarketOrder> marketOrders;

    private CloseableHttpClient client = HttpClientBuilder.create().setMaxConnPerRoute(Region.values().length).build();

    @Scheduled(cron = "0 0/30 * * * ?")
    public void retrieveMarketOrders() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // Delete old market orders
        marketOrderRepository.deleteAllInBatch();
        marketOrderRepository.flush();

        sellableByTypeId = sellableInvTypeRepository.findAll().stream()
            .collect(Collectors.toMap(sellableInvType -> sellableInvType.getInvType().getId(), sellableInvType -> sellableInvType));

        marketOrders = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(Region.values().length);
        Arrays.stream(Region.values())
            .forEach(region -> executor.submit(() -> retrieveMarketOrders(region, CrestBuilder.getMarketOrders(region.getId()), 1)));

        executor.shutdown();

        // Wait for a maximum of 5 minutes
        try {
            executor.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            log.error("Retrieving market orders took too much time", e);
        }

        marketOrders = marketOrderRepository.save(marketOrders);
        marketOrderRepository.flush();
        marketOrderSearchRepository.save(marketOrders);
        stopWatch.stop();
        log.info("Retrieved market orders in {}ms", stopWatch.getTime());
    }

    private void retrieveMarketOrders(Region region, String url, int page) {
        try {
            HttpGet request = new HttpGet(url);
            CloseableHttpResponse response = client.execute(request);

            // Parse json
            JsonObject json = gsonBean.parse(EntityUtils.toString(response.getEntity()));
            JsonArray items = json.getAsJsonArray("items");

            // Save all market orders that are sellable
            marketOrders.addAll(StreamSupport.stream(items.spliterator(), false)
                .map(JsonElement::getAsJsonObject)
                .filter(this::isSellableAndStationIsHub)
                .map(this::createMarketOrder)
                .collect(Collectors.toList()));
            log.info("Market orders {}'s pages : {}/{}", region, page, json.get("pageCount").getAsInt());

            // Retrieve next page
            if (json.has("next")) {
                retrieveMarketOrders(region, json.get("next").getAsJsonObject().get("href").getAsString(), ++page);
            }
        } catch (IOException e) {
            log.error("Error getting market orders from URL", e);
        }
    }

    public List<TradeDTO> buildHubTrades(Station station) {
        return findSellableWithoutSkill()
            .filter(sellableInvType -> marketOrderRepository.countByInvTypeAndStationIDAndBuyFalse(sellableInvType.getInvType(), station.getId()) > 0)
            .map(sellableInvType -> getTradesForAllStations(station, sellableInvType))
            .flatMap(Collection::stream)
            .sorted((t1, t2) -> t2.getPercentProfit().compareTo(t1.getPercentProfit()))
            .collect(Collectors.toList());
    }

    public List<TradeDTO> buildPenuryTrades(Station station) {
        return sellableInvTypeRepository.findAll().stream()
            .filter(sellableInvType -> isPenury(station, sellableInvType))
            .map(sellableInvType -> new TradeDTO(sellableInvType.getInvType(), station))
            .collect(Collectors.toList());
    }

    public List<TradeDTO> buildStationTrades(Station station) {
        Set<Long> userMarket = getInvTypeInUserMarket(station.getId(), 1);

        return findSellableWithoutSkill()
            .map(sellableInvType -> createStationTrade(sellableInvType.getInvType(), station.getId(), userMarket))
            .filter(this::isProfitable)
            .sorted((t1, t2) -> t2.getProfit().compareTo(t1.getProfit()))
            .collect(Collectors.toList());
    }

    private List<TradeDTO> getTradesForAllStations(Station station, SellableInvType sellableInvType) {
        Set<Long> userMarket = getInvTypeInUserMarket(station.getId(), 0);
        Map<Long, List<MarketOrder>> sellOrdersByStation = marketOrderRepository.findByInvTypeAndBuyFalseOrderByPrice(sellableInvType.getInvType())
            .stream()
            .collect(Collectors.groupingBy(MarketOrder::getStationID));
        Double cheapestBuy = sellOrdersByStation.get(station.getId()).get(0).getPrice();

        return Arrays.stream(Station.values())
            .filter(sellStation -> sellStation != station && isCheapestThanBuyStation(sellOrdersByStation.get(sellStation.getId()), cheapestBuy))
            .map(sellStation -> new TradeDTO(sellOrdersByStation.get(sellStation.getId()), userMarket, cheapestBuy))
            .collect(Collectors.toList());
    }

    private boolean isPenury(Station station, SellableInvType sellableInvType) {
        return marketOrderRepository.countByInvTypeAndStationIDAndBuyFalse(sellableInvType.getInvType(), station.getId()) == 0;
    }

    private Stream<SellableInvType> findSellableWithoutSkill() {
        return sellableInvTypeRepository.findByInvTypeInvMarketGroupParentGroupIDNot(SellableInvMarketGroup.SKILLS.getId()).stream();
    }

    private TradeDTO createStationTrade(InvType invType, Long stationID, Set<Long> userMarket) {
        Optional<MarketOrder> cheapestSell = findCheapestSellOrder(invType, stationID);
        Optional<MarketOrder> costliestBuy = findCostliestBuyOrder(invType, stationID);
        if (cheapestSell.isPresent() && costliestBuy.isPresent()) {
            return new TradeDTO(cheapestSell.get(), costliestBuy.get(), userMarket);
        }
        return null;
    }

    private boolean isProfitable(TradeDTO trade) {
        return trade != null && trade.getPercentProfit() >= 10;
    }

    private Optional<MarketOrder> findCheapestSellOrder(InvType invType, Long stationID) {
        return marketOrderRepository.findFirstByInvTypeAndStationIDAndBuyFalseOrderByPrice(invType, stationID);
    }

    private Optional<MarketOrder> findCostliestBuyOrder(InvType invType, Long stationID) {
        return marketOrderRepository.findFirstByInvTypeAndStationIDAndBuyTrueOrderByPriceDesc(invType, stationID);
    }

    private Set<Long> getInvTypeInUserMarket(long stationID, int bid) {
        User user = userService.getUserWithAuthorities();
        if (user.getKeyId() == null || StringUtils.isBlank(user.getVCode())) {
            return new HashSet<>();
        }
        try {
            MarketOrdersParser parser = new MarketOrdersParser();
            ApiAuthorization auth = new ApiAuthorization(user.getKeyId(), user.getVCode());
            MarketOrdersResponse response = parser.getResponse(auth);
            return response.getAll().stream()
                .filter(marketOrder -> isValidOrderFromStation(stationID, bid, marketOrder))
                .mapToLong(com.beimin.eveapi.model.shared.MarketOrder::getTypeID)
                .boxed()
                .collect(Collectors.toSet());
        } catch (ApiException e) {
            log.error("Unable to retrieve user's market orders", e);
        }
        return new HashSet<>();
    }

    private boolean isValidOrderFromStation(long stationID, int bid, com.beimin.eveapi.model.shared.MarketOrder marketOrder) {
        return stationID == marketOrder.getStationID() && marketOrder.getBid() == bid && marketOrder.getOrderState() == 0;
    }

    private boolean isSellableAndStationIsHub(JsonObject item) {
        return sellableByTypeId.containsKey(item.get("type").getAsLong()) && Station.fromLong(item.get("stationID").getAsLong()).isPresent();
    }

    private MarketOrder createMarketOrder(JsonObject item) {
        long typeID = item.get("type").getAsLong();
        MarketOrder marketOrder = new MarketOrder(item);
        marketOrder.setSellableInvType(sellableByTypeId.get(typeID));
        marketOrder.setInvType(invTypeRepository.getOne(typeID));
        return marketOrder;
    }

    private boolean isCheapestThanBuyStation(List<MarketOrder> cheapestSell, Double cheapestBuy) {
        return !CollectionUtils.isEmpty(cheapestSell) && cheapestSell.get(0).getPrice() < cheapestBuy;
    }
}
