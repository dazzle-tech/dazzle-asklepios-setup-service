package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.UomGroupUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UomGroupUnitRepository extends JpaRepository<UomGroupUnit, Long> {
    List<UomGroupUnit> findByGroupId(Long groupId);
}
