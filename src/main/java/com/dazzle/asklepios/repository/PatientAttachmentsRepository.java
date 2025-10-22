package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PatientAttachments;
import com.dazzle.asklepios.domain.enumeration.PatientAttachmentSource;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientAttachmentsRepository extends JpaRepository<PatientAttachments,Long> {
    Page<PatientAttachments> findByPatientIdAndDeletedAtIsNullOrderByCreatedDateDesc(Long patientId, Pageable pageable);

    @Query("select p from PatientAttachments p where p.id = :id and p.deletedAt is null")
    Optional<PatientAttachments> findActiveById(@Param("id") Long id);

    boolean existsByPatientIdAndSpaceKey(Long patientId, String spaceKey);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update PatientAttachments p set p.deletedAt = CURRENT_TIMESTAMP where p.id = :id and p.deletedAt is null")
    int softDelete(@Param("id") Long id);

    Optional<PatientAttachments> findFirstByPatientIdAndSourceAndDeletedAtIsNullOrderByCreatedDateDesc(
            Long patientId, PatientAttachmentSource source
    );
}
