package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PackageSetup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PackageSetupRepository extends JpaRepository<PackageSetup, Long>, JpaSpecificationExecutor<PackageSetup> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
}
