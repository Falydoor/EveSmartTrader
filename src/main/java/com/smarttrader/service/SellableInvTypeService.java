package com.smarttrader.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.smarttrader.domain.InvType;
import com.smarttrader.domain.Referential;
import com.smarttrader.domain.SellableInvType;
import com.smarttrader.domain.enums.Region;
import com.smarttrader.domain.util.CrestBuilder;
import com.smarttrader.domain.util.GsonBean;
import com.smarttrader.repository.InvMarketGroupRepository;
import com.smarttrader.repository.InvTypeRepository;
import com.smarttrader.repository.MarketOrderRepository;
import com.smarttrader.repository.SellableInvTypeRepository;
import com.smarttrader.repository.search.SellableInvTypeSearchRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Service class for managing sellable inv types.
 */
@Service
@Transactional
public class SellableInvTypeService {

    private final Logger log = LoggerFactory.getLogger(SellableInvTypeService.class);

    private final int THREAD_NUMBER = 100;

    @Inject
    private SellableInvTypeRepository sellableInvTypeRepository;

    @Inject
    private SellableInvTypeSearchRepository sellableInvTypeSearchRepository;

    @Inject
    private InvTypeRepository invTypeRepository;

    @Inject
    private InvMarketGroupRepository invMarketGroupRepository;

    @Inject
    private MarketOrderRepository marketOrderRepository;

    @Inject
    private GsonBean gsonBean;

    private List<InvType> marketableInvTypes;

    private CloseableHttpClient client = HttpClientBuilder.create().setMaxConnPerRoute(THREAD_NUMBER).build();

    private AtomicInteger doneNumber;

    private double percent;

    private List<SellableInvType> sellableInvTypes;

    @PostConstruct
    private void init() {
        marketableInvTypes = invTypeRepository.findByInvMarketGroupNotNull().parallelStream().filter(invType -> {
            String result = invMarketGroupRepository.getMainParentMarketGroup(invType.getInvMarketGroup().getId());
            String[] mainParentMarketGroupResult = StringUtils.split(result, '_');
            if (mainParentMarketGroupResult != null && mainParentMarketGroupResult.length > 1) {
                boolean isMarketable = Referential.SELLABLE_PARENT_GROUP.contains(Long.parseLong(mainParentMarketGroupResult[0])) && invType.getVolume() <= 1000;
                if (isMarketable) {
                    Referential.GROUP_PARENT_NAME_BY_TYPE_ID.put(invType.getId(), mainParentMarketGroupResult[1]);
                    return true;
                }
            }
            return false;
        }).collect(Collectors.toList());
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void retrieveSellableInvType() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        doneNumber = new AtomicInteger();
        percent = -1;

        // Delete market orders
        marketOrderRepository.deleteAllInBatch();
        marketOrderRepository.flush();

        // Delete old sellable inv types
        sellableInvTypeRepository.deleteAllInBatch();
        sellableInvTypeRepository.flush();

        sellableInvTypes = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_NUMBER);
        marketableInvTypes.forEach(invType -> executor.submit(() -> getHistory(invType, 0)));

        executor.shutdown();

        // Wait for a maximum of 5 minutes
        try {
            executor.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            log.error("Retrieving sellable inv type took too much time", e);
        }

        log.info("Saving sellable inv type");
        sellableInvTypes = sellableInvTypeRepository.save(sellableInvTypes);
        sellableInvTypeRepository.flush();
        sellableInvTypeSearchRepository.save(sellableInvTypes);
        stopWatch.stop();
        log.info("Retrieved sellable inv type in {}ms", stopWatch.getTime());
    }

    private void getHistory(InvType invType, int tries) {
        String url = CrestBuilder.getHistory(Region.THE_FORGE.getId(), invType.getId());
        try {
            HttpGet request = new HttpGet(url);
            CloseableHttpResponse response = client.execute(request);

            // Parse json
            JsonObject json = gsonBean.parse(EntityUtils.toString(response.getEntity()));
            JsonArray items = json.getAsJsonArray("items");

            // Test history from last month. An item is sellable if 15 history day match the isHistorySellable function
            Boolean isSellable = StreamSupport.stream(items.spliterator(), false)
                .skip(Math.max(0, items.size() - 31))
                .map(JsonElement::getAsJsonObject)
                .filter(this::isHistorySellable)
                .count() > 14;

            if (isSellable) {
                SellableInvType sellableInvType = new SellableInvType();
                sellableInvType.setInvType(invType);
                sellableInvTypes.add(sellableInvType);
            }
            double tempPercent = Math.floor(100 * doneNumber.incrementAndGet() / marketableInvTypes.size());
            if (tempPercent != percent && tempPercent % 5 == 0) {
                percent = tempPercent;
                log.info("Sellable progress : {}%", percent);
            }
        } catch (ConnectTimeoutException e) {
            log.info("Timeout on {} try {}", url, tries);
            if (tries < 6) {
                getHistory(invType, ++tries);
            }
        } catch (IOException e) {
            log.error("Error getting sellable inv types from URL : " + url, e);
        }
    }

    private boolean isHistorySellable(JsonObject history) {
        int volume = history.get("volume").getAsInt();
        double highPrice = history.get("highPrice").getAsDouble();

        // Inv type with a small volume and high price
        if (volume >= 50 && highPrice >= 2000000) {
            return true;
        }
        // Inv type with a high volume and low price
        if (volume >= 500 && highPrice >= 1000000) {
            return true;
        }

        return false;
    }
}
