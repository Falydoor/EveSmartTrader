package com.smarttrader.repository;

import com.smarttrader.domain.InvType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for the InvType entity.
 */
public interface InvTypeRepository extends JpaRepository<InvType, Long> {

    List<InvType> findByInvMarketGroupNotNull();
}
