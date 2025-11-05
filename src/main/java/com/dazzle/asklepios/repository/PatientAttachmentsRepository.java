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

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientAttachmentsRepository extends JpaRepository<PatientAttachments,Long> {
    List<PatientAttachments> findByPatientIdAndDeletedAtIsNullOrderByCreatedDateDesc(Long patientId);

    Optional<PatientAttachments> findByIdAndDeletedAtIsNull(Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update PatientAttachments p set p.deletedAt = CURRENT_TIMESTAMP where p.id = :id and p.deletedAt is null")
    int softDelete(Long id);

    Optional<PatientAttachments> findFirstByPatientIdAndSourceAndDeletedAtIsNullOrderByCreatedDateDesc( Long patientId, PatientAttachmentSource source);
}
