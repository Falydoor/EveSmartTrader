package com.smarttrader.repository;

import com.smarttrader.domain.SellableInvType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for the SellableInvType entity.
 */
public interface SellableInvTypeRepository extends JpaRepository<SellableInvType, Long> {

    List<SellableInvType> findByInvTypeInvMarketGroupParentGroupIDNot(Long parentGroupID);
}
