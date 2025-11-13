package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DentalAction;
import com.dazzle.asklepios.domain.enumeration.DentalActionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link com.dazzle.asklepios.domain.DentalAction}.
 */
@Repository
public interface DentalActionRepository extends JpaRepository<DentalAction, Long> {

    Page<DentalAction> findByType(DentalActionType type , Pageable pageable);

    Page<DentalAction> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);

}
