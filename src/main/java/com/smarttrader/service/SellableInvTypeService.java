package com.smarttrader.service;

import com.smarttrader.domain.InvMarketGroup;
import com.smarttrader.domain.InvType;
import com.smarttrader.domain.Referential;
import com.smarttrader.domain.SellableInvType;
import com.smarttrader.domain.enums.Region;
import com.smarttrader.repository.InvMarketGroupRepository;
import com.smarttrader.repository.InvTypeRepository;
import com.smarttrader.repository.MarketOrderRepository;
import com.smarttrader.repository.SellableInvTypeRepository;
import org.apache.commons.io.IOUtils;
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

    @Scheduled(cron = "0 0 0 * * ?")
    public void retrieveSellableInvType() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // Delete market orders
        marketOrderRepository.deleteAllInBatch();
        marketOrderRepository.flush();

        // Delete old sellable inv types
        sellableInvTypeRepository.deleteAllInBatch();
        sellableInvTypeRepository.flush();

        Set<SellableInvType> sellableInvTypes = new HashSet<>();
        HttpClientBuilder client = HttpClientBuilder.create();
        List<InvType> invTypes = invTypeRepository.findByInvMarketGroupNotNull().parallelStream().filter(invType -> {
            long mainParentGroupID = invType.getInvMarketGroup().getId();
            Long parentGroupID = invType.getInvMarketGroup().getParentGroupID();
            while (parentGroupID != null) {
                InvMarketGroup invMarketGroup = invMarketGroupRepository.findOne(parentGroupID);
                mainParentGroupID = parentGroupID;
                parentGroupID = invMarketGroup.getParentGroupID();
            }
            return Referential.SELLABLE_PARENT_GROUP.contains(mainParentGroupID) && invType.getVolume() <= 1000;
        }).collect(Collectors.toList());
        int[] index = {0};
        double[] percent = {-1};
        invTypes.parallelStream().forEach(invType -> {
            try {
                int j = 0;
                SellableInvType sellableInvType = new SellableInvType();
                sellableInvType.setInvType(invType);
                String url = Referential.CREST_URL + "market/" + Region.THE_FORGE.getId() + "/history/?type=" + Referential.CREST_URL + "inventory/types/" + invType.getId() + "/";
                HttpGet request = new HttpGet(url);
                CloseableHttpResponse response = client.build().execute(request);
                JSONObject jsonObject = new JSONObject(IOUtils.toString(response.getEntity().getContent()));
                JSONArray items = jsonObject.getJSONArray("items");
                for (int i = items.length() - 1; i > Math.max(0, items.length() - 31); --i) {
                    JSONObject item = items.optJSONObject(i);
                    if ((item.getInt("volume") >= 50 && item.getDouble("highPrice") >= 2000000) || (item.getInt("volume") >= 500 && item.getDouble("highPrice") >= 1000000)) {
                        ++j;
                    }
                    if (j > 14) {
                        sellableInvTypes.add(sellableInvType);
                        break;
                    }
                }
                ++index[0];
                double tempPercent = Math.floor(100 * index[0] / invTypes.size());
                if (tempPercent != percent[0]) {
                    percent[0] = tempPercent;
                    log.info("Sellable progress : {}%", percent[0]);
                }
            } catch (IOException e) {
                log.error("Error getting sellable inv types from URL", e);
            } catch (JSONException e) {
                log.error("Error parsing sellable inv types", e);
            }
        });

        log.info("Saving sellable inv type");
        sellableInvTypeRepository.save(sellableInvTypes);
        sellableInvTypeRepository.flush();
        stopWatch.stop();
        log.info("Retrieved sellable inv type in {}ms", stopWatch.getTime());
    }
}
