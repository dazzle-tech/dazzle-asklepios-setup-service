package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.UomGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UomGroupRepository extends JpaRepository<UomGroup, Long> {
    Optional<UomGroup> findByCode(String code);
    boolean existsByCode(String code);
}
