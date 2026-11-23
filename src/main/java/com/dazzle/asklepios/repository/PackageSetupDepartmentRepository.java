package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PackageSetupDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PackageSetupDepartmentRepository extends JpaRepository<PackageSetupDepartment, Long> {

    List<PackageSetupDepartment> findByPackageSetupIdIn(Collection<Long> packageSetupIds);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from PackageSetupDepartment d where d.packageSetupId = :packageSetupId")
    void deleteByPackageSetupId(@Param("packageSetupId") Long packageSetupId);
}
