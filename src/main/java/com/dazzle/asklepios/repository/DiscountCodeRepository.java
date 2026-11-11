package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DiscountCode;
import com.dazzle.asklepios.domain.enumeration.DiscountType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiscountCodeRepository extends JpaRepository<DiscountCode, Long> {

    @Override
    @EntityGraph(attributePaths = "facility")
    Page<DiscountCode> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "facility")
    Optional<DiscountCode> findById(Long id);

    @EntityGraph(attributePaths = "facility")
    Page<DiscountCode> findByFacility_Id(Long facilityId, Pageable pageable);

    @EntityGraph(attributePaths = "facility")
    Page<DiscountCode> findByCodeContainingIgnoreCase(String code, Pageable pageable);

    @EntityGraph(attributePaths = "facility")
    Page<DiscountCode> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @EntityGraph(attributePaths = "facility")
    Page<DiscountCode> findByDiscountType(DiscountType discountType, Pageable pageable);

    boolean existsByFacility_IdAndCodeIgnoreCase(Long facilityId, String code);

    boolean existsByFacility_IdAndCodeIgnoreCaseAndIdNot(Long facilityId, String code, Long id);
}
