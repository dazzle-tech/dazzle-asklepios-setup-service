package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DuplicationCandidate;

import com.dazzle.asklepios.repository.DuplicationCandidateRepository;
import com.dazzle.asklepios.web.rest.vm.DuplicationCandidateCreateVM;
import com.dazzle.asklepios.web.rest.vm.DuplicationCandidateUpdateVM;
import com.dazzle.asklepios.web.rest.vm.DuplicationCandidateResponseVM;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class DuplicationCandidateService {

    private static final Logger LOG = LoggerFactory.getLogger(DuplicationCandidateService.class);

    private final DuplicationCandidateRepository repository;

    public DuplicationCandidateService(DuplicationCandidateRepository repository) {
        this.repository = repository;
    }

    /**
     * Create new candidate
     */
    @CacheEvict(cacheNames = DuplicationCandidateRepository.CACHE_NAME, key = "'all'")
    public DuplicationCandidateResponseVM create(DuplicationCandidateCreateVM vm, String user) {
        LOG.debug("Request to create DuplicationCandidate : {}", vm);

        DuplicationCandidate candidate = new DuplicationCandidate();
        candidate.setDob(vm.dob());
        candidate.setLastName(vm.lastName());
        candidate.setDocumentNo(vm.documentNo());
        candidate.setMobileNumber(vm.mobileNumber());
        candidate.setGender(vm.gender());

        // generate role if not provided
        String role = repository.findMaxRole();
        if (role == null || role.isEmpty()) {
            candidate.setRole("R001");
        } else {
            int lastNumber = Integer.parseInt(role.substring(1));
            candidate.setRole(String.format("R%03d", lastNumber + 1));
        }

        // audit
        candidate.setCreatedBy(user != null ? user : "system");
        candidate.setCreatedDate(Instant.now());

        DuplicationCandidate saved = repository.save(candidate);
        return DuplicationCandidateResponseVM.ofEntity(saved);
    }

    /**
     * Update existing candidate
     */
    @CacheEvict(cacheNames = DuplicationCandidateRepository.CACHE_NAME, key = "'all'")
    public Optional<DuplicationCandidateResponseVM> update(Long id, DuplicationCandidateUpdateVM vm, String user) {
        LOG.debug("Request to update DuplicationCandidate id={} with {}", id, vm);

        return repository.findById(id).map(existing -> {
            if (vm.dob() != null) existing.setDob(vm.dob());
            if (vm.lastName() != null) existing.setLastName(vm.lastName());
            if (vm.documentNo() != null) existing.setDocumentNo(vm.documentNo());
            if (vm.mobileNumber() != null) existing.setMobileNumber(vm.mobileNumber());
            if (vm.gender() != null) existing.setGender(vm.gender());



            existing.setLastModifiedBy(user != null ? user : "system");
            existing.setLastModifiedDate(Instant.now());

            DuplicationCandidate updated = repository.save(existing);
            return DuplicationCandidateResponseVM.ofEntity(updated);
        });
    }
    /**
     * Get all candidates
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = DuplicationCandidateRepository.CACHE_NAME, key = "'all'")
    public List<DuplicationCandidateResponseVM> findAll() {
        LOG.debug("Request to get all DuplicationCandidates");
        return repository.findAll()
                .stream()
                .map(DuplicationCandidateResponseVM::ofEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get one candidate by id
     */
    @Transactional(readOnly = true)
    public Optional<DuplicationCandidateResponseVM> findOne(Long id) {
        LOG.debug("Request to get DuplicationCandidate : {}", id);
        return repository.findById(id)
                .map(DuplicationCandidateResponseVM::ofEntity);
    }

    /**
     * Delete candidate
     */
    @CacheEvict(cacheNames = DuplicationCandidateRepository.CACHE_NAME, key = "'all'")
    public boolean delete(Long id) {
        LOG.debug("Request to delete DuplicationCandidate : {}", id);
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }

}
