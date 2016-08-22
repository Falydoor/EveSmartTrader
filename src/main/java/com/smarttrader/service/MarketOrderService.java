package com.smarttrader.service;

import com.beimin.eveapi.exception.ApiException;
import com.beimin.eveapi.parser.ApiAuthorization;
import com.beimin.eveapi.parser.pilot.MarketOrdersParser;
import com.beimin.eveapi.response.shared.MarketOrdersResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.smarttrader.domain.MarketOrder;
import com.smarttrader.domain.Referential;
import com.smarttrader.domain.SellableInvType;
import com.smarttrader.domain.User;
import com.smarttrader.domain.enums.Region;
import com.smarttrader.domain.enums.Station;
import com.smarttrader.domain.util.GsonBean;
import com.smarttrader.repository.InvTypeRepository;
import com.smarttrader.repository.MarketOrderRepository;
import com.smarttrader.repository.SellableInvTypeRepository;
import com.smarttrader.service.dto.TradeDTO;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
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
    private SellableInvTypeRepository sellableInvTypeRepository;

    @Inject
    private InvTypeRepository invTypeRepository;

    @Inject
    private UserService userService;

    @Inject
    private GsonBean gsonBean;

    private Map<Long, SellableInvType> sellableByTypeId;

    @Scheduled(cron = "0 0/30 * * * ?")
    public void retrieveMarketOrders() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // Delete old market orders
        marketOrderRepository.deleteAllInBatch();
        marketOrderRepository.flush();

        sellableByTypeId = sellableInvTypeRepository.findAll().stream()
            .collect(Collectors.toMap(sellableInvType -> sellableInvType.getInvType().getId(), sellableInvType -> sellableInvType));

        Set<MarketOrder> marketOrders = new HashSet<>();
        Arrays.stream(Region.values()).parallel()
            .forEach(region -> retrieveMarketOrders(marketOrders, region, Referential.CREST_URL + "market/" + region.getId() + "/orders/all/", 1));
        marketOrderRepository.save(marketOrders);
        marketOrderRepository.flush();
        stopWatch.stop();
        log.info("Retrieved market orders in {}ms", stopWatch.getTime());
    }

    private void retrieveMarketOrders(Set<MarketOrder> marketOrders, Region region, String url, int page) {
        try {
            HttpClientBuilder client = HttpClientBuilder.create();
            HttpGet request = new HttpGet(url);
            CloseableHttpResponse response = client.build().execute(request);

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
                retrieveMarketOrders(marketOrders, region, json.get("next").getAsJsonObject().get("href").getAsString(), ++page);
            }
        } catch (IOException e) {
            log.error("Error getting market orders from URL", e);
        }
    }

    public JSONArray buildHubTrades(Station station) {
        Set<Long> invTypeInUserMarket = getInvTypeInUserMarket(station.getId(), 0);
        List<Station> sellStations = Arrays.stream(Station.values()).filter(sellStation -> sellStation != station).collect(Collectors.toList());
        List<TradeDTO> trades = new ArrayList<>();

        sellableInvTypeRepository.findByInvTypeInvMarketGroupParentGroupIDNot(150L).forEach(sellableInvType -> {
            List<MarketOrder> sellOrders = marketOrderRepository.findByInvTypeAndBuyFalseOrderByPrice(sellableInvType.getInvType());
            Map<Long, List<MarketOrder>> sellOrdersByStation = sellOrders.stream().collect(Collectors.groupingBy(MarketOrder::getStationID));
            List<MarketOrder> cheapestBuy = sellOrdersByStation.get(station.getId());

            sellStations.forEach(sellStation -> {
                List<MarketOrder> cheapestSell = sellOrdersByStation.get(sellStation.getId());
                if (!CollectionUtils.isEmpty(cheapestBuy) && !CollectionUtils.isEmpty(cheapestSell) && cheapestSell.get(0).getPrice() < cheapestBuy.get(0).getPrice()) {
                    Double cheapestSellPrice = cheapestSell.get(0).getPrice();
                    Double cheapestBuyPrice = cheapestBuy.get(0).getPrice();
                    List<MarketOrder> sellables = cheapestSell.stream().filter(marketOrder -> marketOrder.getPrice() < Math.min(cheapestSellPrice * 1.1D, cheapestBuyPrice)).collect(Collectors.toList());
                    TradeDTO trade = new TradeDTO();
                    trade.setTotalPrice(Double.valueOf(sellables.stream().mapToDouble(value -> value.getPrice() * value.getVolume()).sum()).longValue());
                    trade.setTotalProfit(Double.valueOf(sellables.stream().mapToDouble(value -> (cheapestBuyPrice - value.getPrice()) * value.getVolume()).sum()).longValue());
                    trade.setTotalQuantity(sellables.stream().mapToLong(MarketOrder::getVolume).sum());
                    trade.setTotalVolume(Double.valueOf(trade.getTotalQuantity() * sellableInvType.getInvType().getVolume()).longValue());
                    trade.setSellPrice(cheapestBuyPrice.longValue());
                    trade.setPercentProfit(100 * trade.getTotalProfit() / trade.getTotalPrice());
                    trade.setProfit(Double.valueOf(cheapestBuyPrice - cheapestSellPrice).longValue());
                    trade.setName(sellableInvType.getInvType().getTypeName());
                    trade.setGroupName(Referential.GROUP_PARENT_NAME_BY_TYPE_ID.get(sellableInvType.getInvType().getId()));
                    trade.setStation(sellStation.toString());
                    trade.setTypeId(sellableInvType.getInvType().getId());
                    trade.setInMarket(invTypeInUserMarket.contains(trade.getTypeId()));
                    trades.add(trade);
                }
            });

        });

        trades.sort((t1, t2) -> t2.getPercentProfit().compareTo(t1.getPercentProfit()));

        return new JSONArray(trades);
    }

    public JSONArray buildPenuryTrades(Station station) {
        List<TradeDTO> trades = new ArrayList<>();

        sellableInvTypeRepository.findAll().stream()
            .filter(sellableInvType -> marketOrderRepository.countByInvTypeAndStationIDAndBuyFalse(sellableInvType.getInvType(), station.getId()) == 0)
            .forEach(sellableInvType -> {
                TradeDTO trade = new TradeDTO();
                trade.setTypeId(sellableInvType.getInvType().getId());
                trade.setName(sellableInvType.getInvType().getTypeName());
                trade.setGroupName(Referential.GROUP_PARENT_NAME_BY_TYPE_ID.get(sellableInvType.getInvType().getId()));
                trade.setStation(station.toString());
                trade.setTotalVolume(sellableInvType.getInvType().getVolume().longValue());
                trades.add(trade);
            });

        return new JSONArray(trades);
    }

    public JSONArray buildStationTrades(Station station) {
        Set<Long> invTypeInUserMarket = getInvTypeInUserMarket(station.getId(), 1);
        List<TradeDTO> trades = new ArrayList<>();

        sellableInvTypeRepository.findByInvTypeInvMarketGroupParentGroupIDNot(150L).forEach(sellableInvType -> {
            Optional<MarketOrder> cheapestSell = marketOrderRepository.findFirstByInvTypeAndStationIDAndBuyFalseOrderByPrice(sellableInvType.getInvType(), station.getId());
            Optional<MarketOrder> costliestBuy = marketOrderRepository.findFirstByInvTypeAndStationIDAndBuyTrueOrderByPriceDesc(sellableInvType.getInvType(), station.getId());

            if (cheapestSell.isPresent() && costliestBuy.isPresent()) {
                TradeDTO trade = new TradeDTO();
                trade.setProfit(Double.valueOf(cheapestSell.get().getPrice() - costliestBuy.get().getPrice()).longValue());
                trade.setSellPrice(costliestBuy.get().getPrice().longValue());
                trade.setPercentProfit(100 * trade.getProfit() / trade.getSellPrice());
                if (trade.getPercentProfit() >= 10) {
                    trade.setName(sellableInvType.getInvType().getTypeName());
                    trade.setGroupName(Referential.GROUP_PARENT_NAME_BY_TYPE_ID.get(sellableInvType.getInvType().getId()));
                    trade.setTypeId(sellableInvType.getInvType().getId());
                    trade.setInMarket(invTypeInUserMarket.contains(trade.getTypeId()));
                    trades.add(trade);
                }
            }
        });

        trades.sort((t1, t2) -> t2.getProfit().compareTo(t1.getProfit()));

        return new JSONArray(trades);
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
                .filter(marketOrder -> stationID == marketOrder.getStationID() && marketOrder.getBid() == bid && marketOrder.getOrderState() == 0)
                .mapToLong(com.beimin.eveapi.model.shared.MarketOrder::getTypeID)
                .boxed()
                .collect(Collectors.toSet());
        } catch (ApiException e) {
            log.error("Unable to retrieve user's market orders", e);
        }
        return new HashSet<>();
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
}
