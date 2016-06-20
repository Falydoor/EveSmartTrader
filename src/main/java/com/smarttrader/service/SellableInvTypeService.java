package com.smarttrader.service;

import com.smarttrader.domain.InvType;
import com.smarttrader.domain.Referential;
import com.smarttrader.domain.SellableInvType;
import com.smarttrader.domain.enums.Region;
import com.smarttrader.repository.InvMarketGroupRepository;
import com.smarttrader.repository.InvTypeRepository;
import com.smarttrader.repository.MarketOrderRepository;
import com.smarttrader.repository.SellableInvTypeRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service class for managing sellable inv types.
 */
@Service
@Transactional
public class SellableInvTypeService {

    private final Logger log = LoggerFactory.getLogger(SellableInvTypeService.class);

    @Inject
    private SellableInvTypeRepository sellableInvTypeRepository;

    @Inject
    private InvTypeRepository invTypeRepository;

    @Inject
    private InvMarketGroupRepository invMarketGroupRepository;

    @Inject
    private MarketOrderRepository marketOrderRepository;

    private List<InvType> marketableInvTypes;

    private CloseableHttpClient client = HttpClientBuilder.create().build();

    private int index;

    private double percent;

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

        index = 0;
        percent = -1;

        // Delete market orders
        marketOrderRepository.deleteAllInBatch();
        marketOrderRepository.flush();

        // Delete old sellable inv types
        sellableInvTypeRepository.deleteAllInBatch();
        sellableInvTypeRepository.flush();

        Set<SellableInvType> sellableInvTypes = new HashSet<>();
        marketableInvTypes.parallelStream().forEach(invType -> {
            String url = Referential.CREST_URL + "market/" + Region.THE_FORGE.getId() + "/history/?type=" + Referential.CREST_URL + "inventory/types/" + invType.getId() + "/";
            getHistory(sellableInvTypes, marketableInvTypes.size(), invType, url, 1);
        });

        log.info("Saving sellable inv type");
        sellableInvTypeRepository.save(sellableInvTypes);
        sellableInvTypeRepository.flush();
        stopWatch.stop();
        log.info("Retrieved sellable inv type in {}ms", stopWatch.getTime());
    }

    private void getHistory(Set<SellableInvType> sellableInvTypes, int invTypesSize, InvType invType, String url, int tries) {
        try {
            SellableInvType sellableInvType = new SellableInvType();
            sellableInvType.setInvType(invType);
            HttpGet request = new HttpGet(url);
            CloseableHttpResponse response = client.execute(request);
            JSONObject jsonObject = new JSONObject(EntityUtils.toString(response.getEntity()));
            JSONArray items = jsonObject.getJSONArray("items");
            List<JSONObject> histories = new ArrayList<>();
            for (int i = items.length() - 1; i > Math.max(0, items.length() - 31); --i) {
                histories.add(items.optJSONObject(i));
            }
            if (histories.stream().filter(this::isHistorySellable).count() > 14) {
                sellableInvTypes.add(sellableInvType);
            }
            ++index;
            double tempPercent = Math.floor(100 * index / invTypesSize);
            if (tempPercent != percent) {
                percent = tempPercent;
                log.info("Sellable progress : {}%", percent);
            }
        } catch (ConnectTimeoutException e) {
            log.info("Timeout on {} try {}", url, tries);
            if (tries < 6) {
                getHistory(sellableInvTypes, invTypesSize, invType, url, ++tries);
            }
        } catch (IOException e) {
            log.error("Error getting sellable inv types from URL", e);
        } catch (JSONException e) {
            log.error("Error parsing sellable inv types", e);
        }
    }

    private boolean isHistorySellable(JSONObject history) {
        try {
            if (history.getInt("volume") >= 50 && history.getDouble("highPrice") >= 2000000) {
                // Inv type with a small volume
                return true;
            } else if (history.getInt("volume") >= 500 && history.getDouble("highPrice") >= 1000000) {
                // Inv type with a high volume
                return true;
            }
        } catch (JSONException e) {
            log.error("Error parsing history", e);
        }
        return false;
    }
}
