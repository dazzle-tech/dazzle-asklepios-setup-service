package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DuplicationCandidate;
import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.enumeration.DayOfWeek;
import com.dazzle.asklepios.repository.DuplicationCandidateRepository;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.service.dto.workingDay.WorkingDayJson;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.facility.FacilityCreateVM;
import com.dazzle.asklepios.web.rest.vm.facility.FacilityResponseVM;
import com.dazzle.asklepios.web.rest.vm.facility.FacilityUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class FacilityService {

    private static final Logger LOG = LoggerFactory.getLogger(FacilityService.class);

    private final FacilityRepository facilityRepository;
    private final DuplicationCandidateRepository duplicationCandidateRepository;

    public FacilityService(
            FacilityRepository facilityRepository,
            DuplicationCandidateRepository duplicationCandidateRepository
    ) {
        this.facilityRepository = facilityRepository;
        this.duplicationCandidateRepository = duplicationCandidateRepository;
    }

    public FacilityResponseVM create(FacilityCreateVM vm) {
        LOG.debug("Request to create Facility : {}", vm);

        Facility facility = new Facility();
        facility.setName(vm.name());
        facility.setType(vm.type());
        facility.setCode(vm.code());
        facility.setEmailAddress(vm.emailAddress());
        facility.setPhone1(vm.phone1());
        facility.setPhone2(vm.phone2());
        facility.setFax(vm.fax());
        facility.setAddressId(vm.addressId());
        facility.setDefaultCurrency(vm.defaultCurrency());
        facility.setTimeZone(vm.timeZone());
        facility.setRegistrationDate(vm.registrationDate());

        validateWorkingDays(vm.workingDays());
        facility.setWorkingDays(vm.workingDays() == null ? List.of() : vm.workingDays());

        Facility saved = facilityRepository.save(facility);
        return FacilityResponseVM.ofEntity(saved);
    }

    public Optional<Facility> update(Long id, FacilityUpdateVM vm) {
        LOG.debug("Request to update Facility id={} with {}", id, vm);

        return facilityRepository.findById(id).map(existing -> {
            if (vm.name() != null) existing.setName(vm.name());
            if (vm.type() != null) existing.setType(vm.type());
            if (vm.code() != null) existing.setCode(vm.code());
            if (vm.emailAddress() != null) existing.setEmailAddress(vm.emailAddress());
            if (vm.registrationDate() != null) existing.setRegistrationDate(vm.registrationDate());
            if (vm.phone1() != null) existing.setPhone1(vm.phone1());
            if (vm.phone2() != null) existing.setPhone2(vm.phone2());
            if (vm.fax() != null) existing.setFax(vm.fax());
            if (vm.addressId() != null) existing.setAddressId(vm.addressId());
            if (vm.isActive() != null) existing.setIsActive(vm.isActive());
            if (vm.defaultCurrency() != null) existing.setDefaultCurrency(vm.defaultCurrency());
            if (vm.timeZone() != null) existing.setTimeZone(vm.timeZone());

            if (vm.ruleId() != null) {
                DuplicationCandidate candidate = new DuplicationCandidate();
                candidate.setId(vm.ruleId());
                existing.setRuleId(candidate.getId());
            } else {
                existing.setRuleId(null);
            }

            if (vm.workingDays() != null) {
                validateWorkingDays(vm.workingDays());
                existing.setWorkingDays(vm.workingDays());
            }

            Facility updated = facilityRepository.save(existing);
            LOG.debug("Facility updated successfully: {}", updated);
            return updated;
        });
    }

    @Transactional(readOnly = true)
    public List<FacilityResponseVM> findAll() {
        LOG.debug("Request to get all Facilities");
        return facilityRepository.findAll()
                .stream()
                .map(FacilityResponseVM::ofEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<FacilityResponseVM> findOne(Long id) {
        LOG.debug("Request to get Facility : {}", id);
        return facilityRepository.findById(id)
                .map(FacilityResponseVM::ofEntity);
    }

    public boolean delete(Long id) {
        LOG.debug("Request to delete Facility : {}", id);
        if (!facilityRepository.existsById(id)) {
            return false;
        }
        facilityRepository.deleteById(id);
        return true;
    }

    @Transactional(readOnly = true)
    public List<FacilityResponseVM> findUnlinkedOrLinkedToRule(Long ruleId) {
        LOG.debug("Request to get all Facilities unlinked or linked to roleId={}", ruleId);

        if (!duplicationCandidateRepository.existsById(ruleId)) {
            LOG.info("Role with id {} not found, returning empty list", ruleId);
            return List.of();
        }

        return facilityRepository.findUnlinkedOrLinkedToRule(ruleId)
                .stream()
                .map(FacilityResponseVM::ofEntity)
                .toList();
    }

    private void validateWorkingDays(List<WorkingDayJson> workingDays) {
        if (workingDays == null || workingDays.isEmpty()) {
            return;
        }

        Set<DayOfWeek> uniqueDays = workingDays.stream()
                .map(WorkingDayJson::getDayOfWeek)
                .collect(Collectors.toSet());

        if (uniqueDays.size() != workingDays.size()) {
            throw new BadRequestAlertException(
                    "Duplicate working day entries",
                    "facilityWorkingDay",
                    "duplicate_day"
            );
        }
    }
}