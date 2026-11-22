package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.UomGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface UomGroupRepository extends JpaRepository<UomGroup, Long>, JpaSpecificationExecutor<UomGroup> {
    Page<UomGroup> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<UomGroup> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);
}
