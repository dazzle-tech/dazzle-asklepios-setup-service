package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.UomGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UomGroupRepository extends JpaRepository<UomGroup, Long> {
    Page<UomGroup> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
