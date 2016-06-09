package com.smarttrader.repository.search;

import com.smarttrader.domain.SellableInvType;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the SellableInvType entity.
 */
public interface SellableInvTypeSearchRepository extends ElasticsearchRepository<SellableInvType, Long> {
}
