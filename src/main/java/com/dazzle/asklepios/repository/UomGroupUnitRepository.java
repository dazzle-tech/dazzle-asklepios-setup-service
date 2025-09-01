package com.dazzle.asklepios.repository;


import com.dazzle.asklepios.domain.UomGroupUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UomGroupUnitRepository extends JpaRepository<UomGroupUnit, Long> {
    String UOMGROUPUNIT = "uomGroupUnit";
}
