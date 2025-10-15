package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.EncounterAttachments;
import com.dazzle.asklepios.domain.PatientAttachments;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface EncounterAttachementsRepository extends JpaRepository<EncounterAttachments,Long> {
    Page<EncounterAttachments> findByEncounterIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long encounterId, Pageable pageable);

    @Query("select e from EncounterAttachments e where e.id = :id and e.deletedAt is null")
    Optional<EncounterAttachments> findActiveById(@Param("id") Long id);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update EncounterAttachments e set e.deletedAt = CURRENT_TIMESTAMP where e.id = :id and e.deletedAt is null")
    int softDelete(@Param("id") Long id);
}
