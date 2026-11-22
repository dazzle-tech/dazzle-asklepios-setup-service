package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PackageSetupItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PackageSetupItemRepository extends JpaRepository<PackageSetupItem, Long> {

    List<PackageSetupItem> findByPackageSetupIdOrderByIdAsc(Long packageSetupId);

    List<PackageSetupItem> findByPackageSetupIdIn(Collection<Long> packageSetupIds);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from PackageSetupItem i where i.packageSetupId = :packageSetupId")
    void deleteByPackageSetupId(@Param("packageSetupId") Long packageSetupId);
}
