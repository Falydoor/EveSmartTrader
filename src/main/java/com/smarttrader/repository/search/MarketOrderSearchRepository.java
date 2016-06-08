package com.smarttrader.repository.search;

import com.smarttrader.domain.MarketOrder;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the MarketOrder entity.
 */
public interface MarketOrderSearchRepository extends ElasticsearchRepository<MarketOrder, Long> {
}
