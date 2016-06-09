package com.smarttrader.service;

import com.smarttrader.domain.InvType;
import com.smarttrader.domain.MarketOrder;
import com.smarttrader.domain.enums.Region;
import com.smarttrader.repository.MarketOrderRepository;
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
import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Service class for managing market orders.
 */
@Service
@Transactional
public class MarketOrderService {

    private final Logger log = LoggerFactory.getLogger(MarketOrderService.class);

    @Inject
    private EntityManager em;

    @Inject
    private MarketOrderRepository marketOrderRepository;

    @Scheduled(cron = "0 0/30 * * * ?")
    public void retrieveMarketOrders() {
        // Delete old market orders
        marketOrderRepository.deleteAll();
        marketOrderRepository.flush();

        Arrays.stream(Region.values()).forEach(region -> {
            retrieveMarketOrders("https://crest-tq.eveonline.com/market/" + region + "/orders/all/");
        });
    }

    private void retrieveMarketOrders(String url) {
        log.info("Market Orders URL : {}", url);
        try {
            HttpClientBuilder client = HttpClientBuilder.create();
            HttpGet request = new HttpGet(url);
            CloseableHttpResponse response = client.build().execute(request);
            JSONObject jsonObject = new JSONObject(IOUtils.toString(response.getEntity().getContent()));

            // Save all market orders
            Set<MarketOrder> marketOrders = new HashSet<>();
            JSONArray items = jsonObject.getJSONArray("items");
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.optJSONObject(i);
                MarketOrder marketOrder = new MarketOrder(item);
                marketOrder.setInvType(em.getReference(InvType.class, item.getLong("type")));
                marketOrders.add(marketOrder);
            }
            marketOrderRepository.save(marketOrders);

            // Retrieve next page
            if (!jsonObject.isNull("next")) {
                retrieveMarketOrders(jsonObject.getJSONObject("next").getString("href"));
            }
        } catch (IOException e) {
            log.error("Error getting market orders from URL", e);
        } catch (JSONException e) {
            log.error("Error parsing market orders", e);
        }
    }

}
