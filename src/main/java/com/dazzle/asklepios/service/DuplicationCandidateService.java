package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DuplicationCandidate;
import com.dazzle.asklepios.repository.DuplicationCandidateRepository;
import com.dazzle.asklepios.repository.FacilityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
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

    /** Create new DuplicationCandidate entity */
    public DuplicationCandidate create(DuplicationCandidate entity, String user) {
        LOG.debug("Request to create DuplicationCandidate: {}", entity);

        // Generate role code automatically
        String lastRole = repository.findMaxRole();
        if (lastRole == null || lastRole.isEmpty()) {
            entity.setRule("R001");
        } else {
            int lastNumber = Integer.parseInt(lastRole.substring(1));
            entity.setRule(String.format("R%03d", lastNumber + 1));
        }


        if (entity.getIsActive() == null) entity.setIsActive(true);
        if (entity.getFields() == null) entity.setFields(Map.of());


        return repository.save(entity);
    }

    /** Update existing DuplicationCandidate */
    public Optional<DuplicationCandidate> update(Long id, DuplicationCandidate updateData, String user) {
        LOG.debug("Request to update DuplicationCandidate id={}", id);

        return repository.findById(id).map(existing -> {

            if (updateData.getFields() != null) existing.setFields(updateData.getFields());
            if (updateData.getIsActive() != null) existing.setIsActive(updateData.getIsActive());
            return repository.save(existing);
        });
    }

    /** Retrieve all candidates */
    @Transactional(readOnly = true)
    public List<DuplicationCandidate> findAll() {
        LOG.debug("Request to get all DuplicationCandidates");
        return repository.findAll();
    }


    /** Retrieve single candidate by id */
    @Transactional(readOnly = true)
    public Optional<DuplicationCandidate> findOne(Long id) {
        return repository.findById(id);
    }

    /** Deactivate candidate */
    public boolean deactivate(Long id, String user) {
        LOG.debug("Request to deactivate DuplicationCandidate: {}", id);
        return repository.findById(id).map(existing -> {
            existing.setIsActive(false);
            repository.save(existing);
            return true;
        }).orElse(false);
    }

    /** Reactivate candidate */
    public boolean reactivate(Long id, String user) {
        LOG.debug("Request to reactivate DuplicationCandidate: {}", id);
        return repository.findById(id).map(existing -> {
            existing.setIsActive(true);
            repository.save(existing);
            return true;
        }).orElse(false);
    }

    /** Search by role fragment */
    @Transactional(readOnly = true)
    public List<DuplicationCandidate> findByRoleFilter(String roleFilter) {
        return repository.findByRoleContaining(roleFilter);
    }

    /** Update fields JSON from Map directly */
    public Optional<DuplicationCandidate> updateFields(Long id, Map<String, Boolean> fields, String user) {
        return repository.findById(id).map(existing -> {
            existing.setFields(fields);
            return repository.save(existing);
        });
    }

    /** Hard delete if needed */
    public void delete(Long id) {
        LOG.warn("Deleting DuplicationCandidate id={}", id);
        repository.deleteById(id);
    }
}
