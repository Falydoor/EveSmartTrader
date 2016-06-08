package com.smarttrader.repository.search;

import com.smarttrader.domain.InvType;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the InvType entity.
 */
public interface InvTypeSearchRepository extends ElasticsearchRepository<InvType, Long> {
}
