package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DuplicationCandidate;
import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.repository.DuplicationCandidateRepository;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.web.rest.vm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DuplicationCandidateService {

    private static final Logger LOG = LoggerFactory.getLogger(DuplicationCandidateService.class);

    private final DuplicationCandidateRepository repository;
    private final FacilityRepository facilityRepository;

    public DuplicationCandidateService(DuplicationCandidateRepository repository,
                                       FacilityRepository facilityRepository) {
        this.repository = repository;
        this.facilityRepository = facilityRepository;
    }

    @CacheEvict(cacheNames = DuplicationCandidateRepository.CACHE_NAME, key = "'all'")
    public DuplicationCandidateResponseVM create(DuplicationCandidateCreateVM vm, String user) {
        LOG.debug("Request to create DuplicationCandidate : {}", vm);

        DuplicationCandidate entity = new DuplicationCandidate();
        entity.setDob(vm.dob());
        entity.setLastName(vm.lastName());
        entity.setDocumentNo(vm.documentNo());
        entity.setMobileNumber(vm.mobileNumber());
        entity.setGender(vm.gender());
        entity.setActive(vm.active() != null ? vm.active() : true);


        if (vm.facilityId() != null) {
            Facility facility = facilityRepository.findById(vm.facilityId())
                    .orElseThrow(() -> new IllegalArgumentException("Facility not found with id " + vm.facilityId()));
            entity.setFacility(facility);
        } else {
            entity.setFacility(null);
        }


        String lastRole = repository.findMaxRole();
        if (lastRole == null || lastRole.isEmpty()) {
            entity.setRole("R001");
        } else {
            int lastNumber = Integer.parseInt(lastRole.substring(1));
            entity.setRole(String.format("R%03d", lastNumber + 1));
        }

        // audit
        entity.setCreatedBy(user != null ? user : "system");
        entity.setCreatedDate(Instant.now());

        DuplicationCandidate saved = repository.save(entity);
        return DuplicationCandidateResponseVM.ofEntity(saved);
    }

    @CacheEvict(cacheNames = DuplicationCandidateRepository.CACHE_NAME, key = "'all'")
    public Optional<DuplicationCandidateResponseVM> update(Long id, DuplicationCandidateUpdateVM vm, String user) {
        LOG.debug("Request to update DuplicationCandidate id={} : {}", id, vm);

        return repository.findById(id).map(existing -> {
            if (vm.dob() != null) existing.setDob(vm.dob());
            if (vm.lastName() != null) existing.setLastName(vm.lastName());
            if (vm.documentNo() != null) existing.setDocumentNo(vm.documentNo());
            if (vm.mobileNumber() != null) existing.setMobileNumber(vm.mobileNumber());
            if (vm.gender() != null) existing.setGender(vm.gender());
            if (vm.active() != null) existing.setActive(vm.active());
            if (vm.facilityId() != null) {
                Facility facility = facilityRepository.findById(vm.facilityId())
                        .orElseThrow(() -> new IllegalArgumentException("Facility not found with id " + vm.facilityId()));
                existing.setFacility(facility);
            }

            existing.setLastModifiedBy(user != null ? user : "system");
            existing.setLastModifiedDate(Instant.now());

            DuplicationCandidate updated = repository.save(existing);
            return DuplicationCandidateResponseVM.ofEntity(updated);
        });
    }

    @Transactional(readOnly = true)
    public List<DuplicationCandidateResponseVM> findAll() {
        LOG.debug("Request to get all DuplicationCandidates");
        return repository.findAll().stream()
                .map(DuplicationCandidateResponseVM::ofEntity)
                .toList();
    }

    @CacheEvict(cacheNames = DuplicationCandidateRepository.CACHE_NAME, key = "'all'")
    public boolean deactivate(Long id, String user) {
        LOG.debug("Request to deactivate DuplicationCandidate : {}", id);
        return repository.findById(id).map(existing -> {
            existing.setActive(false);
            existing.setLastModifiedBy(user != null ? user : "system");
            existing.setLastModifiedDate(Instant.now());
            repository.save(existing);
            return true;
        }).orElse(false);
    }

    public boolean reactivate(Long id, String user) {
        LOG.debug("Request to reactivate DuplicationCandidate : {}", id);
        return repository.findById(id).map(existing -> {
            existing.setActive(true);
            existing.setLastModifiedBy(user != null ? user : "system");
            existing.setLastModifiedDate(Instant.now());
            repository.save(existing);
            return true;
        }).orElse(false);
    }

    public List<DuplicationCandidateResponseVM> findByRoleFilter(String roleFilter) {
        return repository.findByRoleContaining(roleFilter) // أو findByRoleStartingWith(roleFilter)
                .stream()
                .map(DuplicationCandidateResponseVM::ofEntity)
                .toList();
    }


}
