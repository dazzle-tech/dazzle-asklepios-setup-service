package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PatientAttachments;
import com.dazzle.asklepios.domain.enumeration.PatientAttachmentSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientAttachmentsRepository extends JpaRepository<PatientAttachments,Long> {
    List<PatientAttachments> findByPatientIdAndDeletedAtIsNullOrderByCreatedDateDesc(Long patientId);

    Optional<PatientAttachments> findByIdAndDeletedAtIsNull(Long id);

    Optional<PatientAttachments> findFirstByPatientIdAndSourceAndDeletedAtIsNullOrderByCreatedDateDesc( Long patientId, PatientAttachmentSource source);
}
