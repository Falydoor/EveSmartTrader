package com.smarttrader.repository;

import com.smarttrader.domain.SellableInvType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.stream.Stream;

/**
 * Spring Data JPA repository for the SellableInvType entity.
 */
public interface SellableInvTypeRepository extends JpaRepository<SellableInvType, Long> {

    Stream<SellableInvType> findByInvTypeInvMarketGroupParentGroupIDNot(Long parentGroupID);

    @Query(value = "SELECT * FROM sellable_inv_type WHERE inv_type_id NOT IN (SELECT DISTINCT inv_type_id FROM market_order WHERE buy = FALSE AND station_id = :station)", nativeQuery = true)
    Stream<SellableInvType> findSellPenury(@Param("station") Long station);
}
