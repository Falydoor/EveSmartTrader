package com.smarttrader.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.smarttrader.domain.InvType;
import com.smarttrader.domain.MarketOrder;
import com.smarttrader.domain.SellableInvType;
import com.smarttrader.domain.enums.Region;
import com.smarttrader.domain.enums.SellableInvMarketGroup;
import com.smarttrader.domain.enums.Station;
import com.smarttrader.domain.util.CrestBuilder;
import com.smarttrader.domain.util.GsonBean;
import com.smarttrader.repository.InvTypeRepository;
import com.smarttrader.repository.MarketOrderRepository;
import com.smarttrader.repository.SellableInvTypeRepository;
import com.smarttrader.repository.search.MarketOrderSearchRepository;
import com.smarttrader.security.SecurityUtils;
import com.smarttrader.service.builder.TradeBuilder;
import com.smarttrader.service.dto.TradeDTO;
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

import javax.inject.Inject;
import java.io.IOException;
import java.math.BigInteger;
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

    public List<TradeDTO> buildHubTrades() {
        List<Long> idsNotPenury = getInvTypesNotPenury();
        return Stream.concat(getSellOrders(idsNotPenury), getCheapestSellOrders(idsNotPenury))
            .collect(Collectors.groupingBy(MarketOrder::getInvType))
            .values().stream()
            .map(this::getTradesForAllStations)
            .flatMap(Collection::stream)
            .sorted((t1, t2) -> t2.getPercentProfit().compareTo(t1.getPercentProfit()))
            .collect(Collectors.toList());
    }

    public List<TradeDTO> buildPenuryTrades() {
        return sellableInvTypeRepository.findSellPenury(SecurityUtils.getBuyId())
            .map(TradeDTO::new)
            .collect(Collectors.toList());
    }

    public List<TradeDTO> buildStationTrades() {
        return findSellableWithoutSkill()
            .map(this::createStationTrade)
            .filter(this::isProfitable)
            .sorted((t1, t2) -> t2.getProfit().compareTo(t1.getProfit()))
            .collect(Collectors.toList());
    }

    private List<Long> getInvTypesNotPenury() {
        return sellableInvTypeRepository.findSellNotPenury(SecurityUtils.getBuyId(), Collections.singletonList(SellableInvMarketGroup.SKILLS.getId()))
            .map(BigInteger::longValue)
            .collect(Collectors.toList());
    }

    private Stream<MarketOrder> getSellOrders(List<Long> idsNotPenury) {
        return marketOrderRepository.findByInvTypeIdInAndStationIDNotAndBuyFalseOrderByPrice(idsNotPenury, SecurityUtils.getBuyId());
    }

    private Stream<MarketOrder> getCheapestSellOrders(List<Long> idsNotPenury) {
        return marketOrderRepository.findCheapestSellOrder(idsNotPenury, SecurityUtils.getBuyId());
    }

    private List<TradeDTO> getTradesForAllStations(List<MarketOrder> marketOrders) {
        TradeBuilder tradeBuilder = new TradeBuilder(marketOrders.stream());
        return Arrays.stream(Station.values())
            .filter(tradeBuilder::isCheapestThanBuyStation)
            .collect(Collectors.mapping(tradeBuilder::getTrade, Collectors.toList()));
    }

    private Stream<InvType> findSellableWithoutSkill() {
        return sellableInvTypeRepository.findByInvTypeInvMarketGroupParentGroupIDNot(SellableInvMarketGroup.SKILLS.getId()).map(SellableInvType::getInvType);
    }

    private TradeDTO createStationTrade(InvType invType) {
        Optional<MarketOrder> cheapestSell = findCheapestSellOrder(invType);
        Optional<MarketOrder> costliestBuy = findCostliestBuyOrder(invType);
        if (cheapestSell.isPresent() && costliestBuy.isPresent()) {
            return new TradeDTO(cheapestSell.get(), costliestBuy.get());
        }
        return null;
    }

    private boolean isProfitable(TradeDTO trade) {
        return trade != null && trade.getPercentProfit() >= 10;
    }

    private Optional<MarketOrder> findCheapestSellOrder(InvType invType) {
        return marketOrderRepository.findFirstByInvTypeAndStationIDAndBuyFalseOrderByPrice(invType, SecurityUtils.getBuyId());
    }

    private Optional<MarketOrder> findCostliestBuyOrder(InvType invType) {
        return marketOrderRepository.findFirstByInvTypeAndStationIDAndBuyTrueOrderByPriceDesc(invType, SecurityUtils.getBuyId());
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
