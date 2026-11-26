package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PayorSupportiveDocumentRequirement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PayorSupportiveDocumentRequirementRepository
        extends JpaRepository<PayorSupportiveDocumentRequirement, Long> {

    Optional<PayorSupportiveDocumentRequirement> findByPayorId(Long payorId);
}
