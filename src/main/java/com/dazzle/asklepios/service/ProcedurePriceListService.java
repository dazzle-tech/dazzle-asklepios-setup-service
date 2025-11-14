package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Procedure;
import com.dazzle.asklepios.domain.ProcedurePriceList;
import com.dazzle.asklepios.repository.ProcedurePriceListRepository;
import com.dazzle.asklepios.repository.ProcedureRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

@Service
@Transactional
public class ProcedurePriceListService {

    private static final Logger LOG = LoggerFactory.getLogger(ProcedurePriceListService.class);

    private final ProcedurePriceListRepository repository;
    private final ProcedureRepository procedureRepository;

    public ProcedurePriceListService(
            ProcedurePriceListRepository repository,
            ProcedureRepository procedureRepository
    ) {
        this.repository = repository;
        this.procedureRepository = procedureRepository;
    }

    // ====================== CREATE ======================
    public ProcedurePriceList create(Long procedureId, ProcedurePriceList input) {
        LOG.debug("Request to create ProcedurePriceList for procedureId={} payload={}", procedureId, input);

        if (procedureId == null) {
            throw new BadRequestAlertException("Procedure ID is required", "procedurePriceList", "procedureid.required");
        }
        if (input == null) {
            throw new BadRequestAlertException("ProcedurePriceList payload is required", "procedurePriceList", "payload.required");
        }
        if (input.getCurrency() == null || input.getCurrency().isBlank()) {
            throw new BadRequestAlertException("currency is required", "procedurePriceList", "currency.required");
        }
        if (input.getPrice() == null) {
            throw new BadRequestAlertException("price is required", "procedurePriceList", "price.required");
        }
        if (input.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestAlertException("price must be >= 0", "procedurePriceList", "price.invalid");
        }

        Procedure procedure = procedureRepository.findById(procedureId)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Procedure not found with id " + procedureId, "procedure", "notfound"
                ));

        ProcedurePriceList entity = ProcedurePriceList.builder()
                .procedure(procedure)
                .price(input.getPrice())
                .currency(input.getCurrency().trim())
                .build();

        try {
            ProcedurePriceList saved = repository.saveAndFlush(entity);
            LOG.debug("Created ProcedurePriceList: {}", saved);
            return saved;
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            String msg = extractMessage(ex);
            LOG.warn("DB constraint violation while creating ProcedurePriceList (procedureId={}, currency={}, price={}): {}",
                    procedureId, input.getCurrency(), input.getPrice(), msg, ex);

            if (isUniqueViolation(msg, "uk_procedurepricelist_procedure_currency_price")) {
                throw new BadRequestAlertException(
                        "A ProcedurePriceList with the same (procedure, currency, price) already exists.",
                        "procedurePriceList",
                        "unique.procedurepricelist"
                );
            }
            throw new BadRequestAlertException(
                    "Database constraint violated while creating ProcedurePriceList.",
                    "procedurePriceList",
                    "db.constraint"
            );
        }
    }

    @Transactional(readOnly = true)
    public Page<ProcedurePriceList> findByProcedureId(Long procedureId, Pageable pageable) {
        LOG.debug("Request to get ProcedurePriceList by procedureId={} pageable={}", procedureId, pageable);
        if (procedureId == null) {
            throw new BadRequestAlertException("Procedure ID is required", "procedurePriceList", "procedureid.required");
        }
        return repository.findByProcedureId(procedureId, pageable);
    }

    public void delete(Long id) {
        LOG.debug("Request to delete ProcedurePriceList : {}", id);

        if (!repository.existsById(id)) {
            throw new NotFoundAlertException("ProcedurePriceList not found with id " + id, "procedurePriceList", "notfound");
        }

        repository.deleteById(id);
        LOG.debug("Deleted ProcedurePriceList id={}", id);
    }

    // ====================== Helpers ======================
    private String extractMessage(Throwable ex) {
        Throwable root = getRootCause(ex);
        return (root != null && root.getMessage() != null ? root.getMessage() : ex.getMessage());
    }

    private boolean isUniqueViolation(String lowerMsg, String expectedConstraintName) {
        if (lowerMsg == null) return false;
        String m = lowerMsg.toLowerCase();
        return m.contains(expectedConstraintName.toLowerCase())
                || m.contains("unique constraint")
                || m.contains("duplicate key")
                || m.contains("duplicate entry");
    }
}
