package com.smarttrader.repository.search;

import com.smarttrader.domain.InvMarketGroup;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the InvMarketGroup entity.
 */
public interface InvMarketGroupSearchRepository extends ElasticsearchRepository<InvMarketGroup, Long> {
}
