package com.smarttrader.repository;

import com.smarttrader.domain.SellableInvType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Stream;

/**
 * Spring Data JPA repository for the SellableInvType entity.
 */
public interface SellableInvTypeRepository extends JpaRepository<SellableInvType, Long> {

    Stream<SellableInvType> findByInvTypeInvMarketGroupParentGroupIDNot(Long parentGroupID);

    @Query(value = "SELECT * FROM sellable_inv_type " +
        "WHERE inv_type_id NOT IN (SELECT DISTINCT inv_type_id FROM market_order WHERE buy = FALSE AND station_id = :station)", nativeQuery = true)
    Stream<SellableInvType> findSellPenury(@Param("station") Long station);

    @Query(value = "SELECT DISTINCT sit.inv_type_id FROM sellable_inv_type sit " +
        "INNER JOIN inv_type it ON sit.inv_type_id = it.id " +
        "INNER JOIN market_order mo ON it.id = mo.inv_type_id " +
        "INNER JOIN inv_market_group img ON it.inv_market_group_id = img.id " +
        "WHERE mo.station_id = :station AND mo.buy = FALSE AND img.parent_group_id NOT IN (:excludedGroups)", nativeQuery = true)
    Stream<BigInteger> findSellNotPenury(@Param("station") Long station, @Param("excludedGroups") List<Long> excludedGroups);
}
