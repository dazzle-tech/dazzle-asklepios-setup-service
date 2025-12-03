package com.dazzle.asklepios.service;


import com.dazzle.asklepios.domain.ReferralRequest;
import com.dazzle.asklepios.domain.enumeration.ReferralType;
import com.dazzle.asklepios.repository.ReferralRequestRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.referral.ReferralRequestSaveVM;
import com.dazzle.asklepios.web.rest.vm.referral.ReferralRequestUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReferralRequestService {

    private static final Logger LOG = LoggerFactory.getLogger(ReferralRequestService.class);

    private final ReferralRequestRepository repo;

    public ReferralRequestService(ReferralRequestRepository repo) {
        this.repo = repo;
    }

    // ------------ CREATE ------------
    public ReferralRequest create(ReferralRequestSaveVM vm) {
        LOG.debug("Create ReferralRequest payload={}", vm);

        ReferralType type = vm.referralType() != null ? vm.referralType() : ReferralType.INTERNAL;

        validateTypeRules(type, vm.facilityId(), vm.departmentId());
        validateMandatory(vm.referralReason(), vm.priority());

        ReferralRequest r = ReferralRequest.builder()
                .patientId(vm.patientId())
                .encounterId(vm.encounterId())
                .referralType(type)
                .facilityId(vm.facilityId())
                .departmentId(vm.departmentId())
                .referralReason(vm.referralReason())
                .priority(vm.priority())
                .isActive(vm.isActive() != null ? vm.isActive() : true)
                .build();

        return repo.save(r);
    }

    // ------------ UPDATE ------------
    public ReferralRequest update(ReferralRequestUpdateVM vm) {
        LOG.debug("Update ReferralRequest payload={}", vm);

        ReferralRequest existing = repo.findById(vm.id())
                .orElseThrow(() -> new BadRequestAlertException(
                        "notFound",
                        "referralRequest",
                        "Referral Request not found."
                ));

        validateTypeRules(vm.referralType(), vm.facilityId(), vm.departmentId());
        validateMandatory(vm.referralReason(), vm.priority());

        existing.setPatientId(vm.patientId());
        existing.setEncounterId(vm.encounterId());
        existing.setReferralType(vm.referralType());

        existing.setFacilityId(vm.facilityId());
        existing.setDepartmentId(vm.departmentId());
        existing.setReferralReason(vm.referralReason());
        existing.setPriority(vm.priority());
        existing.setIsActive(vm.isActive() != null ? vm.isActive() : existing.getIsActive());

        return repo.save(existing);
    }

    private void validateTypeRules(ReferralType type, Long facilityId, Long departmentId) {
        if (type == null) {
            throw new BadRequestAlertException(
                    "invalidType",
                    "referralRequest",
                    "Referral type is mandatory."
            );
        }

        if (departmentId == null) {
            throw new BadRequestAlertException(
                    "departmentMandatory",
                    "referralRequest",
                    "Department is mandatory."
            );
        }

        if (type == ReferralType.EXTERNAL && facilityId == null) {
            throw new BadRequestAlertException(
                    "facilityMandatory",
                    "referralRequest",
                    "Facility is mandatory for external referral."
            );
        }
    }

    private void validateMandatory(String reason, Object priority) {
        if (reason == null || reason.isBlank()) {
            throw new BadRequestAlertException(
                    "reasonMandatory",
                    "referralRequest",
                    "Referral reason is mandatory."
            );
        }
        if (priority == null) {
            throw new BadRequestAlertException(
                    "priorityMandatory",
                    "referralRequest",
                    "Priority is mandatory."
            );
        }
    }

    // ------------ READ ------------
    @Transactional(readOnly = true)
    public Page<ReferralRequest> findAll(Pageable pageable) {
        return repo.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<ReferralRequest> getByPatient(Long patientId, Pageable pageable) {
        return repo.findByPatientId(patientId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ReferralRequest> getByPatientAndEncounter(Long patientId, Long encounterId, Pageable pageable) {
        return repo.findByPatientIdAndEncounterId(patientId, encounterId, pageable);
    }

    @Transactional(readOnly = true)
    public java.util.Optional<ReferralRequest> findOne(Long id) {
        return repo.findById(id);
    }
}
