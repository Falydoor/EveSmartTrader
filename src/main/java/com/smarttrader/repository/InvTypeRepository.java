package com.smarttrader.repository;

import com.smarttrader.domain.InvType;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the InvType entity.
 */
public interface InvTypeRepository extends JpaRepository<InvType,Long> {

}
