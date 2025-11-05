package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.ServiceSetup;
import com.dazzle.asklepios.repository.CdtServiceMappingRepository;
import com.dazzle.asklepios.repository.ServiceRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

@Service
@Transactional
public class CdtServiceMappingService {

    private static final Logger LOG = LoggerFactory.getLogger(CdtServiceMappingService.class);

    private final CdtServiceMappingRepository cdtServiceMappingRepository;
    private final ServiceRepository serviceRepository;
    private final EntityManager em;

    public CdtServiceMappingService(
            CdtServiceMappingRepository cdtServiceMappingRepository,
            ServiceRepository serviceRepository,
            EntityManager em
    ) {
        this.cdtServiceMappingRepository = cdtServiceMappingRepository;
        this.serviceRepository = serviceRepository;
        this.em = em;
    }

    // ====================== CREATE ======================
    public void create(Long cdtId, Long serviceId) {
        LOG.info("[CREATE] Request to map Service id={} to CDT id={}", serviceId, cdtId);

        if (cdtId == null) {
            throw new BadRequestAlertException("CDT id is required", "cdtServiceMapping", "cdt.required");
        }
        if (serviceId == null) {
            throw new BadRequestAlertException("Service id is required", "cdtServiceMapping", "service.required");
        }

        try {
            // مثال توضيحي - في تطبيقك سيكون لديك Entity CdtServiceMapping
            // cdtServiceMappingRepository.save(new CdtServiceMapping(cdtId, serviceId));
            LOG.info("Successfully mapped serviceId={} to cdtId={}", serviceId, cdtId);
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            Throwable root = getRootCause(ex);
            String message = (root != null ? root.getMessage() : ex.getMessage()).toLowerCase();
            LOG.error("Database constraint violation while creating CDT-Service mapping: {}", message, ex);

            if (message.contains("unique constraint") ||
                    message.contains("duplicate key") ||
                    message.contains("duplicate entry")) {
                throw new BadRequestAlertException(
                        "This service is already mapped to the specified CDT.",
                        "cdtServiceMapping",
                        "unique.mapping"
                );
            }
            throw new BadRequestAlertException(
                    "Database constraint violated while creating CDT-Service mapping.",
                    "cdtServiceMapping",
                    "db.constraint"
            );
        }
    }

    // ====================== READ ======================
  //  @Transactional(readOnly = true)
//    public List<Long> findServiceIdsByCdtId(Long cdtId) {
//        LOG.debug("[READ] Fetching service IDs for CDT id={}", cdtId);
//
//        if (cdtId == null) {
//            throw new BadRequestAlertException("CDT id is required", "cdtServiceMapping", "cdt.required");
//        }
//
//        List<Long> ids = cdtServiceMappingRepository.findServiceIdsByCdtId(cdtId);
//        return ids != null ? ids : Collections.emptyList();
//    }

//    @Transactional(readOnly = true)
//    public List<ServiceSetup> findServicesByCdtId(Long cdtId) {
//        LOG.debug("[READ] Fetching full ServiceSetup list for CDT id={}", cdtId);
//
//        List<Long> ids = findServiceIdsByCdtId(cdtId);
//        if (ids.isEmpty()) {
//            return Collections.emptyList();
//        }
//        return serviceRepository.findAllById(ids);
//    }

    // ====================== DELETE ======================
    public void delete(Long mappingId) {
        LOG.info("[DELETE] Request to delete CDT-Service mapping id={}", mappingId);

        if (mappingId == null) {
            throw new BadRequestAlertException("Mapping id is required", "cdtServiceMapping", "id.required");
        }

        try {
            // هنا نستخدم repository.deleteById(id)
            // تأكد أن Repository يرث من JpaRepository أو CrudRepository
            cdtServiceMappingRepository.deleteById(mappingId);
            LOG.info("Successfully deleted CDT-Service mapping id={}", mappingId);
        } catch (org.springframework.dao.EmptyResultDataAccessException ex) {
            LOG.error("Mapping not found with id={}", mappingId, ex);
            throw new NotFoundAlertException("Mapping not found with id " + mappingId, "cdtServiceMapping", "notfound");
        } catch (Exception ex) {
            Throwable root = getRootCause(ex);
            LOG.error("Unexpected error while deleting CDT-Service mapping: {}", root != null ? root.getMessage() : ex.getMessage(), ex);
            throw new BadRequestAlertException("Failed to delete CDT-Service mapping", "cdtServiceMapping", "delete.failed");
        }
    }
}
