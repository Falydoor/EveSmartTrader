package com.smarttrader.service;

import com.smarttrader.domain.InvType;
import com.smarttrader.domain.MarketOrder;
import com.smarttrader.domain.Referential;
import com.smarttrader.domain.SellableInvType;
import com.smarttrader.domain.enums.Region;
import com.smarttrader.domain.enums.Station;
import com.smarttrader.repository.InvTypeRepository;
import com.smarttrader.repository.MarketOrderRepository;
import com.smarttrader.repository.SellableInvTypeRepository;
import org.apache.commons.io.IOUtils;
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

    @Scheduled(cron = "0 0/30 * * * ?")
    public void retrieveMarketOrders() {
        // Delete old market orders
        marketOrderRepository.deleteAllInBatch();
        marketOrderRepository.flush();

        List<SellableInvType> sellableInvTypes = sellableInvTypeRepository.findAll();

        Arrays.stream(Region.values()).parallel()
            .forEach(region -> retrieveMarketOrders(region, sellableInvTypes, "https://crest-tq.eveonline.com/market/" + region.getId() + "/orders/all/", 1));
        marketOrderRepository.flush();
    }

    private void retrieveMarketOrders(Region region, List<SellableInvType> sellableInvTypes, String url, int page) {
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
                SellableInvType sellableInvType = new SellableInvType();
                sellableInvType.setId(typeID);
                if (!sellableInvTypes.contains(sellableInvType) || Station.fromLong(item.getLong("stationID")) != Station.getStationWithRegion(region)) {
                    continue;
                }
                MarketOrder marketOrder = new MarketOrder(item);
                marketOrder.setSellableInvType(sellableInvTypeRepository.getOne(typeID));
                marketOrder.setInvType(invTypeRepository.getOne(typeID));
                marketOrders.add(marketOrder);
            }
            marketOrderRepository.save(marketOrders);
            log.info("Market orders {}'s pages : {}/{}", region, page, jsonObject.getInt("pageCount"));

            // Retrieve next page
            if (!jsonObject.isNull("next")) {
                retrieveMarketOrders(region, sellableInvTypes, jsonObject.getJSONObject("next").getString("href"), ++page);
            }
        } catch (IOException e) {
            log.error("Error getting market orders from URL", e);
        } catch (JSONException e) {
            log.error("Error parsing market orders", e);
        }
    }

}
