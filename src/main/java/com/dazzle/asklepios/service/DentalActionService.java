package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DentalAction;
import com.dazzle.asklepios.domain.enumeration.DentalActionType;
import com.dazzle.asklepios.repository.DentalActionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DentalActionService {

    private final DentalActionRepository repository;

    public DentalActionService(DentalActionRepository repository) {
        this.repository = repository;
    }

    public DentalAction create(DentalAction dentalAction) {
        return repository.save(dentalAction);
    }

    public Optional<DentalAction> update(Long id, DentalAction dentalAction) {
        return repository.findById(id).map(existing -> {
            existing.setDescription(dentalAction.getDescription());
            existing.setType(dentalAction.getType());
            existing.setImageName(dentalAction.getImageName());
            existing.setIsActive(dentalAction.getIsActive());
            return repository.save(existing);
        });
    }

    public Page<DentalAction> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Optional<DentalAction> findOne(Long id) {
        return repository.findById(id);
    }

    public Page<DentalAction> findByType(DentalActionType type, Pageable pageable) {
        return repository.findByType(type,pageable);
    }

    public Page<DentalAction> findByDescription(String description,Pageable pageable) {
        return repository.findByDescriptionContainingIgnoreCase(description,pageable);
    }
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
