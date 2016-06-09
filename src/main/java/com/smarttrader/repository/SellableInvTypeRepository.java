package com.smarttrader.repository;

import com.smarttrader.domain.SellableInvType;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the SellableInvType entity.
 */
public interface SellableInvTypeRepository extends JpaRepository<SellableInvType,Long> {

}
