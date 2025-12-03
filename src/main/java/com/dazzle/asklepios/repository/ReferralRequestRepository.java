package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.ReferralRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReferralRequestRepository extends JpaRepository<ReferralRequest, Long> {

    Page<ReferralRequest> findByPatientId(Long patientId, Pageable pageable);

    Page<ReferralRequest> findByPatientIdAndEncounterId(Long patientId, Long encounterId, Pageable pageable);
}

