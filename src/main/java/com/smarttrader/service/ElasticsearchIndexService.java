package com.smarttrader.service;

import com.codahale.metrics.annotation.Timed;
import com.smarttrader.domain.*;
import com.smarttrader.repository.*;
import com.smarttrader.repository.search.*;
import org.elasticsearch.indices.IndexAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.List;

@Service
public class ElasticsearchIndexService {

    private final Logger log = LoggerFactory.getLogger(ElasticsearchIndexService.class);

    @Inject
    private InvMarketGroupRepository invMarketGroupRepository;

    @Inject
    private InvMarketGroupSearchRepository invMarketGroupSearchRepository;

    @Inject
    private InvTypeRepository invTypeRepository;

    @Inject
    private InvTypeSearchRepository invTypeSearchRepository;

    @Inject
    private MarketOrderRepository marketOrderRepository;

    @Inject
    private MarketOrderSearchRepository marketOrderSearchRepository;

    @Inject
    private SellableInvTypeRepository sellableInvTypeRepository;

    @Inject
    private SellableInvTypeSearchRepository sellableInvTypeSearchRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserSearchRepository userSearchRepository;

    @Inject
    private ElasticsearchTemplate elasticsearchTemplate;

    @Async
    @Timed
    public void reindexAll() {
        reindexForClass(InvMarketGroup.class, invMarketGroupRepository, invMarketGroupSearchRepository);
        reindexForClass(InvType.class, invTypeRepository, invTypeSearchRepository);
        reindexForClass(MarketOrder.class, marketOrderRepository, marketOrderSearchRepository);
        reindexForClass(SellableInvType.class, sellableInvTypeRepository, sellableInvTypeSearchRepository);
        reindexForClass(User.class, userRepository, userSearchRepository);

        log.info("Elasticsearch: Successfully performed reindexing");
    }

    @Transactional
    @SuppressWarnings("unchecked")
    private <T> void reindexForClass(Class<T> entityClass, JpaRepository<T, Long> jpaRepository,
                                     ElasticsearchRepository<T, Long> elasticsearchRepository) {
        elasticsearchTemplate.deleteIndex(entityClass);
        try {
            elasticsearchTemplate.createIndex(entityClass);
        } catch (IndexAlreadyExistsException e) {
            // Do nothing. Index was already concurrently recreated by some other service.
        }
        elasticsearchTemplate.putMapping(entityClass);
        if (jpaRepository.count() > 0) {
            try {
                Method m = jpaRepository.getClass().getMethod("findAllWithEagerRelationships");
                elasticsearchRepository.save((List<T>) m.invoke(jpaRepository));
            } catch (Exception e) {
                elasticsearchRepository.save(jpaRepository.findAll());
            }
        }
        log.info("Elasticsearch: Indexed all rows for " + entityClass.getSimpleName());
    }
}
