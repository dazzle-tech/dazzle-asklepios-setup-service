package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.CdtCode;
import com.dazzle.asklepios.domain.CdtServiceMapping;
import com.dazzle.asklepios.domain.ServiceSetup;
import com.dazzle.asklepios.repository.CdtCodeRepository;
import com.dazzle.asklepios.repository.CdtServiceMappingRepository;
import com.dazzle.asklepios.service.dto.CdtServiceMappingSyncResultDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CdtServiceMappingService {

    private final CdtServiceMappingRepository mappingRepository;
    private final CdtCodeRepository cdtRepository;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<Long> getLinkedServiceIds(Long cdtId) {
        return mappingRepository.findByCdtCode_Id(cdtId).stream()
                .map(m -> m.getService().getId())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceSetup> getLinkedServices(Long cdtId) {
        return mappingRepository.findByCdtCode_Id(cdtId).stream()
                .map(CdtServiceMapping::getService)
                .toList();
    }

    public CdtServiceMappingSyncResultDTO sync(Long cdtId, List<Long> serviceIds) {
        if (cdtId == null) {
            throw new BadRequestAlertException("cdtId is required", "cdtService", "cdt.required");
        }

        CdtCode cdtCode = cdtRepository.findById(cdtId)
                .orElseThrow(() -> new NotFoundAlertException("CDT not found: " + cdtId, "cdtService", "cdt.notfound"));

        List<Long> existingServiceIds = mappingRepository.findByCdtCode_Id(cdtId).stream()
                .map(m -> m.getService().getId())
                .toList();
        Integer beforeCount = existingServiceIds.size();

        Set<Long> desiredServiceIds = (serviceIds == null) ? Set.of() : new HashSet<>(serviceIds);

        Integer removedCount;
        if (desiredServiceIds.isEmpty()) {
            mappingRepository.deleteAllByCdtCode_Id(cdtId);
            removedCount = beforeCount;
        } else {
            mappingRepository.deleteByCdtCode_IdAndService_IdNotIn(cdtId, desiredServiceIds);
            removedCount = (int) existingServiceIds.stream()
                    .filter(id -> !desiredServiceIds.contains(id))
                    .count();
        }

        Integer addedCount = 0;
        for (Long serviceId : desiredServiceIds) {
            if (!existingServiceIds.contains(serviceId)) {
                CdtServiceMapping mapping = CdtServiceMapping.builder()
                        .cdtCode(entityManager.getReference(CdtCode.class, cdtCode.getId()))
                        .service(entityManager.getReference(ServiceSetup.class, serviceId))
                        .build();
                mappingRepository.save(mapping);
                addedCount++;
            }
        }

        Integer afterCount = mappingRepository.findByCdtCode_Id(cdtId).size();
        log.info("[CDT-SYNC] cdtId={} added={} removed={} after={}", cdtId, addedCount, removedCount, afterCount);

        return new CdtServiceMappingSyncResultDTO(beforeCount, addedCount, removedCount, afterCount);
    }
}
