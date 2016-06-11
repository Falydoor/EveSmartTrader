package com.smarttrader.repository;

import com.smarttrader.domain.MarketOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the MarketOrder entity.
 */
public interface MarketOrderRepository extends JpaRepository<MarketOrder, Long> {

    Optional<MarketOrder> findFirstByInvTypeIdAndStationIDAndBuyFalseOrderByPrice(Long invTypeId, Long stationID);

    List<MarketOrder> findByInvTypeIdAndStationIDAndBuyFalseAndPriceLessThanEqualOrderByPrice(Long invTypeId, Long stationID, Double price);

    Long countByInvTypeIdAndStationIDAndBuyFalse(Long invTypeId, Long stationID);

    Optional<MarketOrder> findFirstByInvTypeIdAndStationIDAndBuyTrueOrderByPriceDesc(Long invTypeId, Long stationID);
}
