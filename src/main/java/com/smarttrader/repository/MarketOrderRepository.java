package com.smarttrader.repository;

import com.smarttrader.domain.InvType;
import com.smarttrader.domain.MarketOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Spring Data JPA repository for the MarketOrder entity.
 */
public interface MarketOrderRepository extends JpaRepository<MarketOrder, Long> {
    Optional<MarketOrder> findFirstByInvTypeAndStationIDAndBuyFalseOrderByPrice(InvType invType, Long stationID);

    Long countByInvTypeAndStationIDAndBuyFalse(InvType invType, Long stationID);

    Optional<MarketOrder> findFirstByInvTypeAndStationIDAndBuyTrueOrderByPriceDesc(InvType invType, Long stationID);

    Stream<MarketOrder> findByInvTypeIdInAndBuyFalseOrderByPrice(List<Long> invTypes);
}
