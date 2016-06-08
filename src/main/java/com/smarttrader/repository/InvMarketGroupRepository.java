package com.smarttrader.repository;

import com.smarttrader.domain.InvMarketGroup;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the InvMarketGroup entity.
 */
public interface InvMarketGroupRepository extends JpaRepository<InvMarketGroup,Long> {

}
