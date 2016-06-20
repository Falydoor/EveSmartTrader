package com.smarttrader.repository;

import com.smarttrader.domain.InvMarketGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for the InvMarketGroup entity.
 */
public interface InvMarketGroupRepository extends JpaRepository<InvMarketGroup, Long> {

    @Query(value = "SELECT get_main_parent_market_group(:id)", nativeQuery = true)
    String getMainParentMarketGroup(@Param("id") Long id);
}
