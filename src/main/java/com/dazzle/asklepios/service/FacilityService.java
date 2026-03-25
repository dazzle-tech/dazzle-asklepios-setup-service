package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DuplicationCandidate;
import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.FacilityWorkingDay;
import com.dazzle.asklepios.domain.enumeration.DayOfWeek;
import com.dazzle.asklepios.repository.DuplicationCandidateRepository;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.repository.FacilityWorkingDayRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.facility.FacilityCreateVM;
import com.dazzle.asklepios.web.rest.vm.facility.FacilityResponseVM;
import com.dazzle.asklepios.web.rest.vm.facility.FacilityUpdateVM;
import com.dazzle.asklepios.web.rest.vm.facility.FacilityWorkingDayCreateVM;
import com.dazzle.asklepios.web.rest.vm.facility.FacilityWorkingDayUpdateVM;
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
    private final FacilityWorkingDayRepository facilityWorkingDayRepository;

    public FacilityService(FacilityRepository facilityRepository, DuplicationCandidateRepository duplicationCandidateRepository, FacilityWorkingDayRepository facilityWorkingDayRepository) {
        this.facilityRepository = facilityRepository;
        this.duplicationCandidateRepository = duplicationCandidateRepository;
        this.facilityWorkingDayRepository = facilityWorkingDayRepository;
    }

//     @CacheEvict(cacheNames = FacilityRepository.FACILITIES, key = "'all'")
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

        Facility saved = facilityRepository.save(facility);
        saveWorkingDaysOnCreate(saved, vm.workingDays());
        loadWorkingDays(saved);
        return FacilityResponseVM.ofEntity(saved);
    }


//    @CacheEvict(cacheNames = FacilityRepository.FACILITIES, key = "'all'")
    public Optional<Facility> update(Long id, FacilityUpdateVM vm) {
        LOG.debug("Request to update Facility id={} with {}", id, vm);

        return facilityRepository.findById(id).map(existing -> {
            if (vm.name() != null) existing.setName(vm.name());
            if (vm.type() != null) existing.setType(vm.type());
            if (vm.emailAddress() != null) existing.setEmailAddress(vm.emailAddress());
            if (vm.registrationDate() != null) existing.setRegistrationDate(vm.registrationDate());
            if (vm.phone1() != null) existing.setPhone1(vm.phone1());
            if (vm.phone2() != null) existing.setPhone2(vm.phone2());
            if (vm.fax() != null) existing.setFax(vm.fax());
            if (vm.addressId() != null) existing.setAddressId(vm.addressId());
            if (vm.isActive() != null) existing.setIsActive(vm.isActive());
            existing.setDefaultCurrency(vm.defaultCurrency());
            if (vm.ruleId() != null) {

                DuplicationCandidate candidate = new DuplicationCandidate();
                candidate.setId(vm.ruleId());
                existing.setRuleId(candidate.getId());
            } else {

                existing.setRuleId(null);
            }
            if (vm.timeZone() != null) existing.setTimeZone(vm.timeZone());

            Facility updated = facilityRepository.save(existing);
            if (vm.workingDays() != null) {
                replaceWorkingDays(updated, vm.workingDays());
            }

            loadWorkingDays(updated);
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

//     @CacheEvict(cacheNames = FacilityRepository.FACILITIES, key = "'all'")
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

    private void saveWorkingDaysOnCreate(Facility facility, List<FacilityWorkingDayCreateVM> workingDays) {
        if (workingDays == null || workingDays.isEmpty()) {
            facility.setWorkingDays(List.of());
            return;
        }

        validateCreateWorkingDays(workingDays);

        List<FacilityWorkingDay> entities = workingDays.stream()
                .map(item -> {
                    FacilityWorkingDay row = new FacilityWorkingDay();
                    row.setFacility(facility);
                    row.setDayOfWeek(item.dayOfWeek());
                    row.setIsWorking(item.isWorking());
                    return row;
                })
                .toList();

        facilityWorkingDayRepository.saveAll(entities);

    }

    private void replaceWorkingDays(Facility facility, List<FacilityWorkingDayUpdateVM> workingDays) {
        validateUpdateWorkingDays(workingDays);

        facilityWorkingDayRepository.deleteAllByFacilityId(facility.getId());
        facilityWorkingDayRepository.flush();

        if (workingDays.isEmpty()) {
            facility.setWorkingDays(List.of());
            return;
        }

        List<FacilityWorkingDay> entities = workingDays.stream()
                .map(item -> {
                    FacilityWorkingDay row = new FacilityWorkingDay();
                    row.setFacility(facility);
                    row.setDayOfWeek(item.dayOfWeek());
                    row.setIsWorking(item.isWorking());
                    return row;
                })
                .toList();

        facilityWorkingDayRepository.saveAll(entities);
        facilityWorkingDayRepository.flush();
    }

    private void validateCreateWorkingDays(List<FacilityWorkingDayCreateVM> workingDays) {
        Set<DayOfWeek> uniqueDays = workingDays.stream()
                .map(FacilityWorkingDayCreateVM::dayOfWeek)
                .collect(Collectors.toSet());

        if (uniqueDays.size() != workingDays.size()) {
            throw new BadRequestAlertException(
                    "Duplicate working day entries",
                    "facilityWorkingDay",
                    "duplicate_day"
            );
        }
    }

    private void validateUpdateWorkingDays(List<FacilityWorkingDayUpdateVM> workingDays) {
        Set<DayOfWeek> uniqueDays = workingDays.stream()
                .map(FacilityWorkingDayUpdateVM::dayOfWeek)
                .collect(Collectors.toSet());

        if (uniqueDays.size() != workingDays.size()) {
            throw new BadRequestAlertException(
                    "Duplicate working day entries",
                    "facilityWorkingDay",
                    "duplicate_day"
            );
        }
    }

    private void loadWorkingDays(Facility facility) {
        List<FacilityWorkingDay> workingDays =
                facilityWorkingDayRepository.findAllByFacilityIdOrderByDayOfWeekAsc(
                        facility.getId()
                );
        facility.setWorkingDays(workingDays);
    }


}
