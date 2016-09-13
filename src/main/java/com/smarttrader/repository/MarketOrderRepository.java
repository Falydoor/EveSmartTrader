package com.smarttrader.repository;

import com.smarttrader.domain.MarketOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.stream.Stream;

/**
 * Spring Data JPA repository for the MarketOrder entity.
 */
public interface MarketOrderRepository extends JpaRepository<MarketOrder, Long> {
    Stream<MarketOrder> findByInvTypeIdInAndStationIDNotAndBuyFalseOrderByPrice(List<Long> invTypes, Long stationID);

    @Query(value = "SELECT DISTINCT ON (station_id, inv_type_id) * " +
        "FROM market_order " +
        "WHERE inv_type_id IN (:invTypes) AND station_id = :station AND buy = FALSE " +
        "ORDER BY station_id, inv_type_id, price", nativeQuery = true)
    Stream<MarketOrder> findCheapestSellOrder(@Param("invTypes") List<Long> invTypes, @Param("station") Long station);

    @Query(value = "SELECT DISTINCT ON (station_id, inv_type_id) * " +
        "FROM market_order " +
        "WHERE inv_type_id IN (:invTypes) AND station_id = :station AND buy = TRUE " +
        "ORDER BY station_id, inv_type_id, price DESC", nativeQuery = true)
    Stream<MarketOrder> findCostliestBuyOrder(@Param("invTypes") List<Long> invTypes, @Param("station") Long station);
}
