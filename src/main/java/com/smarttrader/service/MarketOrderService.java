package com.smarttrader.service;

import com.beimin.eveapi.exception.ApiException;
import com.beimin.eveapi.parser.ApiAuthorization;
import com.beimin.eveapi.parser.pilot.MarketOrdersParser;
import com.beimin.eveapi.response.shared.MarketOrdersResponse;
import com.smarttrader.domain.MarketOrder;
import com.smarttrader.domain.Referential;
import com.smarttrader.domain.SellableInvType;
import com.smarttrader.domain.User;
import com.smarttrader.domain.enums.Region;
import com.smarttrader.domain.enums.Station;
import com.smarttrader.repository.InvTypeRepository;
import com.smarttrader.repository.MarketOrderRepository;
import com.smarttrader.repository.SellableInvTypeRepository;
import com.smarttrader.service.dto.TradeDTO;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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

    @Scheduled(cron = "0 0/30 * * * ?")
    public void retrieveMarketOrders() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // Delete old market orders
        marketOrderRepository.deleteAllInBatch();
        marketOrderRepository.flush();

        Map<Long, SellableInvType> sellableByTypeId = sellableInvTypeRepository.findAll().stream()
            .collect(Collectors.toMap(sellableInvType -> sellableInvType.getInvType().getId(), sellableInvType -> sellableInvType));

        Arrays.stream(Region.values()).parallel()
            .forEach(region -> retrieveMarketOrders(region, sellableByTypeId, "https://crest-tq.eveonline.com/market/" + region.getId() + "/orders/all/", 1));
        marketOrderRepository.flush();
        stopWatch.stop();
        log.info("Retrieved market orders in {}ms", stopWatch.getTime());
    }

    private void retrieveMarketOrders(Region region, Map<Long, SellableInvType> sellableByTypeId, String url, int page) {
        try {
            HttpClientBuilder client = HttpClientBuilder.create();
            HttpGet request = new HttpGet(url);
            CloseableHttpResponse response = client.build().execute(request);
            JSONObject jsonObject = new JSONObject(IOUtils.toString(response.getEntity().getContent()));

            // Save all market orders that are sellable
            Set<MarketOrder> marketOrders = new HashSet<>();
            JSONArray items = jsonObject.getJSONArray("items");
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.optJSONObject(i);
                long typeID = item.getLong("type");
                SellableInvType sellableInvType = sellableByTypeId.get(typeID);
                if (sellableInvType == null || Station.fromLong(item.getLong("stationID")) != Station.getStationWithRegion(region)) {
                    continue;
                }
                MarketOrder marketOrder = new MarketOrder(item);
                marketOrder.setSellableInvType(sellableInvType);
                marketOrder.setInvType(invTypeRepository.getOne(typeID));
                marketOrders.add(marketOrder);
            }
            marketOrderRepository.save(marketOrders);
            log.info("Market orders {}'s pages : {}/{}", region, page, jsonObject.getInt("pageCount"));

            // Retrieve next page
            if (!jsonObject.isNull("next")) {
                retrieveMarketOrders(region, sellableByTypeId, jsonObject.getJSONObject("next").getString("href"), ++page);
            }
        } catch (IOException e) {
            log.error("Error getting market orders from URL", e);
        } catch (JSONException e) {
            log.error("Error parsing market orders", e);
        }
    }

    public JSONArray buildHubTrades() {
        Station sellStation = Station.AmarrHUB;
        Station buyStation = Station.JitaHUB;

        Set<Long> invTypeInUserMarket = getInvTypeInUserMarket(buyStation.getId(), 0);
        List<TradeDTO> trades = new ArrayList<>();

        sellableInvTypeRepository.findByInvTypeInvMarketGroupParentGroupIDNot(150L).forEach(sellableInvType -> {
            Optional<MarketOrder> cheapestSell = marketOrderRepository.findFirstByInvTypeIdAndStationIDAndBuyFalseOrderByPrice(sellableInvType.getInvType().getId(), sellStation.getId());
            Optional<MarketOrder> cheapestBuy = marketOrderRepository.findFirstByInvTypeIdAndStationIDAndBuyFalseOrderByPrice(sellableInvType.getInvType().getId(), buyStation.getId());

            if (cheapestSell.isPresent() && cheapestBuy.isPresent() && cheapestSell.get().getPrice() < cheapestBuy.get().getPrice()) {
                Double cheapestSellPrice = cheapestSell.get().getPrice();
                Double cheapestBuyPrice = cheapestBuy.get().getPrice();
                List<MarketOrder> sellables = marketOrderRepository.findByInvTypeIdAndStationIDAndBuyFalseAndPriceLessThanEqualAndPriceLessThanOrderByPrice(sellableInvType.getInvType().getId(), sellStation.getId(), cheapestSellPrice * 1.1D, cheapestBuyPrice);
                TradeDTO trade = new TradeDTO();
                trade.setTotalPrice(Double.valueOf(sellables.stream().mapToDouble(value -> value.getPrice() * value.getVolume()).sum()).longValue());
                trade.setTotalProfit(Double.valueOf(sellables.stream().mapToDouble(value -> (cheapestBuyPrice - value.getPrice()) * value.getVolume()).sum()).longValue());
                trade.setTotalQuantity(sellables.stream().mapToLong(MarketOrder::getVolume).sum());
                trade.setTotalVolume(Double.valueOf(trade.getTotalQuantity() * sellableInvType.getInvType().getVolume()).longValue());
                trade.setSellPrice(cheapestBuyPrice.longValue());
                trade.setPercentProfit(100 * trade.getTotalProfit() / trade.getTotalPrice());
                trade.setProfit(Double.valueOf(cheapestBuyPrice - cheapestSellPrice).longValue());
                trade.setName(sellableInvType.getInvType().getTypeName());
                trade.setGroupName(Referential.groupParentNameByTypeId.get(sellableInvType.getInvType().getId()));
                trade.setStation(sellStation.toString());
                trade.setTypeId(sellableInvType.getInvType().getId());
                trade.setInMarket(invTypeInUserMarket.contains(trade.getTypeId()));
                trades.add(trade);
            }
        });

        trades.sort((t1, t2) -> t2.getPercentProfit().compareTo(t1.getPercentProfit()));

        return new JSONArray(trades);
    }

    public JSONArray buildPenuryTrades() {
        Station penuryStation = Station.JitaHUB;

        List<TradeDTO> trades = new ArrayList<>();

        sellableInvTypeRepository.findAll().stream()
            .filter(sellableInvType -> marketOrderRepository.countByInvTypeIdAndStationIDAndBuyFalse(sellableInvType.getInvType().getId(), penuryStation.getId()) == 0)
            .forEach(sellableInvType -> {
                TradeDTO trade = new TradeDTO();
                trade.setTypeId(sellableInvType.getInvType().getId());
                trade.setName(sellableInvType.getInvType().getTypeName());
                trade.setGroupName(Referential.groupParentNameByTypeId.get(sellableInvType.getInvType().getId()));
                trade.setStation(penuryStation.toString());
                trade.setTotalVolume(sellableInvType.getInvType().getVolume().longValue());
                trades.add(trade);
            });

        return new JSONArray(trades);
    }

    public JSONArray buildStationTrades() {
        Station stationTrade = Station.JitaHUB;

        Set<Long> invTypeInUserMarket = getInvTypeInUserMarket(stationTrade.getId(), 1);
        List<TradeDTO> trades = new ArrayList<>();

        sellableInvTypeRepository.findByInvTypeInvMarketGroupParentGroupIDNot(150L).forEach(sellableInvType -> {
            Optional<MarketOrder> cheapestSell = marketOrderRepository.findFirstByInvTypeIdAndStationIDAndBuyFalseOrderByPrice(sellableInvType.getInvType().getId(), stationTrade.getId());
            Optional<MarketOrder> costliestBuy = marketOrderRepository.findFirstByInvTypeIdAndStationIDAndBuyTrueOrderByPriceDesc(sellableInvType.getInvType().getId(), stationTrade.getId());

            if (cheapestSell.isPresent() && costliestBuy.isPresent()) {
                TradeDTO trade = new TradeDTO();
                trade.setProfit(Double.valueOf(cheapestSell.get().getPrice() - costliestBuy.get().getPrice()).longValue());
                trade.setSellPrice(costliestBuy.get().getPrice().longValue());
                trade.setPercentProfit(100 * trade.getProfit() / trade.getSellPrice());
                if (trade.getPercentProfit() >= 10) {
                    trade.setName(sellableInvType.getInvType().getTypeName());
                    trade.setGroupName(Referential.groupParentNameByTypeId.get(sellableInvType.getInvType().getId()));
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
}
