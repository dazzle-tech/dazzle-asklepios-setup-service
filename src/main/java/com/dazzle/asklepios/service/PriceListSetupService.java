package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.PriceListSetup;
import com.dazzle.asklepios.domain.enumeration.PriceListSetupStatus;
import com.dazzle.asklepios.repository.PriceListSetupRepository;
import com.dazzle.asklepios.service.dto.PriceListSetupDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PriceListSetupService {

    private final PriceListSetupRepository priceListSetupRepository;

    public PriceListSetupDTO create(PriceListSetupDTO dto) {

        PriceListSetup entity = new PriceListSetup();

        entity.setFacilityId(dto.facilityId());
        entity.setType(dto.type());
        entity.setPayerId(dto.payerId());
        entity.setName(dto.name());
        entity.setDescription(dto.description());
        entity.setVersionNumber(dto.versionNumber());
        entity.setEffectiveFrom(dto.effectiveFrom());
        entity.setEffectiveTo(dto.effectiveTo());
        entity.setCurrencyCode(dto.currencyCode());

        entity.setStatus(PriceListSetupStatus.DRAFT);
        entity.setIsActive(true);

        PriceListSetup savedEntity =
                priceListSetupRepository.save(entity);

        return toDTO(savedEntity);
    }

    public PriceListSetupDTO update(
            Long id,
            PriceListSetupDTO dto
    ) {

        PriceListSetup entity =
                priceListSetupRepository.findById(id)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Price list setup not found with id: " + id
                                )
                        );

        entity.setFacilityId(dto.facilityId());
        entity.setType(dto.type());
        entity.setPayerId(dto.payerId());
        entity.setName(dto.name());
        entity.setDescription(dto.description());
        entity.setVersionNumber(dto.versionNumber());
        entity.setEffectiveFrom(dto.effectiveFrom());
        entity.setEffectiveTo(dto.effectiveTo());
        entity.setCurrencyCode(dto.currencyCode());

        PriceListSetup savedEntity =
                priceListSetupRepository.save(entity);

        return toDTO(savedEntity);
    }

    @Transactional(readOnly = true)
    public PriceListSetupDTO findById(Long id) {

        PriceListSetup entity =
                priceListSetupRepository.findById(id)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Price list setup not found with id: " + id
                                )
                        );

        return toDTO(entity);
    }

    @Transactional(readOnly = true)
    public Page<PriceListSetupDTO> findAll(
            Pageable pageable
    ) {

        return priceListSetupRepository
                .findAll(pageable)
                .map(this::toDTO);
    }

    public void delete(Long id) {

        if (!priceListSetupRepository.existsById(id)) {
            throw new EntityNotFoundException(
                    "Price list setup not found with id: " + id
            );
        }

        priceListSetupRepository.deleteById(id);
    }

    private PriceListSetupDTO toDTO(
            PriceListSetup entity
    ) {

        return new PriceListSetupDTO(
                entity.getId(),
                entity.getFacilityId(),
                entity.getType(),
                entity.getPayerId(),
                entity.getName(),
                entity.getDescription(),
                entity.getVersionNumber(),
                entity.getEffectiveFrom(),
                entity.getEffectiveTo(),
                entity.getCurrencyCode()
        );
    }
}