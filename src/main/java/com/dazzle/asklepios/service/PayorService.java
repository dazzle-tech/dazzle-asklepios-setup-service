package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Payor;
import com.dazzle.asklepios.domain.enumeration.biling.PayorCategory;
import com.dazzle.asklepios.repository.PayorRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.payor.CchiPayorUpsertVM;
import com.dazzle.asklepios.web.rest.vm.payor.PayorSaveVM;
import com.dazzle.asklepios.web.rest.vm.payor.PayorUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.apache.commons.lang3.StringUtils.firstNonBlank;

@Service
@Transactional
public class PayorService {

    private static final Logger LOG = LoggerFactory.getLogger(PayorService.class);

    private final PayorRepository repo;

    public PayorService(PayorRepository repo) {
        this.repo = repo;
    }

    // ------------ CREATE ------------
    public Payor create(PayorSaveVM vm) {
        LOG.debug("Create Payor payload={}", vm);

        if (repo.existsByCodeIgnoreCase(vm.code())) {
            throw new BadRequestAlertException(
                    "codeExists",
                    "payor",
                    "Payor code already exists."
            );
        }

        validateDates(vm.startDate(), vm.expiryDate());

        Payor p = Payor.builder()
                .code(vm.code())
                .name(vm.name())
                .category(vm.category())

                .address(vm.address())
                .phone(vm.phone())
                .email(vm.email())
                .contractManagerContact(vm.contractManagerContact())

                .startDate(vm.startDate())
                .expiryDate(vm.expiryDate())
                .renewable(vm.renewable() != null ? vm.renewable() : false)
                .nphiesId(vm.nphiesId())
                .waseelPayerId(vm.waseelPayerId())
                .tpaNphiesId(vm.tpaNphiesId())
                .isWaseelEnabled(vm.isWaseelEnabled() != null ? vm.isWaseelEnabled() : false)
                .allowPartialCoverage(bool(vm.allowPartialCoverage()))
                .acceptCopay(bool(vm.acceptCopay()))
                .acceptDeductibles(bool(vm.acceptDeductibles()))
                .allowPackagePricing(bool(vm.allowPackagePricing()))
                .allowDrgBilling(bool(vm.allowDrgBilling()))
                .forcePreApproval(bool(vm.forcePreApproval()))

                .isActive(vm.isActive() != null ? vm.isActive() : true)
                .build();

        return repo.save(p);
    }

    // ------------ UPDATE ------------
    public Payor update(PayorUpdateVM vm) {
        LOG.debug("Update Payor payload={}", vm);

        Payor existing = repo.findById(vm.id())
                .orElseThrow(() -> new BadRequestAlertException(
                        "notFound",
                        "payor",
                        "Payor not found."
                ));
        if (!existing.getCode().equalsIgnoreCase(vm.code())
                && repo.existsByCodeIgnoreCase(vm.code())) {
            throw new BadRequestAlertException(
                    "codeExists",
                    "payor",
                    "Payor code already exists."
            );
        }

        validateDates(vm.startDate(), vm.expiryDate());

        existing.setCode(vm.code());
        existing.setName(vm.name());
        existing.setCategory(vm.category());

        existing.setAddress(vm.address());
        existing.setPhone(vm.phone());
        existing.setEmail(vm.email());
        existing.setContractManagerContact(vm.contractManagerContact());

        existing.setStartDate(vm.startDate());
        existing.setExpiryDate(vm.expiryDate());
        existing.setRenewable(vm.renewable() != null ? vm.renewable() : existing.getRenewable());
        existing.setNphiesId(vm.nphiesId());
        existing.setWaseelPayerId(vm.waseelPayerId());
        existing.setTpaNphiesId(vm.tpaNphiesId());
        existing.setIsWaseelEnabled(vm.isWaseelEnabled() != null ? vm.isWaseelEnabled() : existing.getIsWaseelEnabled());
        existing.setAllowPartialCoverage(vm.allowPartialCoverage() != null ? vm.allowPartialCoverage() : existing.getAllowPartialCoverage());
        existing.setAcceptCopay(vm.acceptCopay() != null ? vm.acceptCopay() : existing.getAcceptCopay());
        existing.setAcceptDeductibles(vm.acceptDeductibles() != null ? vm.acceptDeductibles() : existing.getAcceptDeductibles());
        existing.setAllowPackagePricing(vm.allowPackagePricing() != null ? vm.allowPackagePricing() : existing.getAllowPackagePricing());
        existing.setAllowDrgBilling(vm.allowDrgBilling() != null ? vm.allowDrgBilling() : existing.getAllowDrgBilling());
        existing.setForcePreApproval(vm.forcePreApproval() != null ? vm.forcePreApproval() : existing.getForcePreApproval());

        existing.setIsActive(vm.isActive() != null ? vm.isActive() : existing.getIsActive());

        return repo.save(existing);
    }

    private void validateDates(java.time.LocalDate start, java.time.LocalDate expiry) {
        if (start != null && expiry != null && expiry.isBefore(start)) {
            throw new BadRequestAlertException(
                    "invalidDates",
                    "payor",
                    "Expiry date cannot be before start date."
            );
        }
    }

    private boolean bool(Boolean v) {
        return v != null && v;
    }

    // ------------ READ / SEARCH ------------
    @Transactional(readOnly = true)
    public Page<Payor> search(PayorCategory category, String name, String code, Pageable pageable) {

        String safeName = name == null ? "" : name;
        String safeCode = code == null ? "" : code;

        if (category != null) {
            // 3 filters
            if ((name != null && !name.isBlank()) || (code != null && !code.isBlank())) {
                return repo.findByCategoryAndNameContainingIgnoreCaseAndCodeContainingIgnoreCase(
                        category, safeName, safeCode, pageable
                );
            }
            // فقط category
            return repo.findByCategory(category, pageable);
        }

        if (name != null && !name.isBlank()) {
            return repo.findByNameContainingIgnoreCase(safeName, pageable);
        }

        if (code != null && !code.isBlank()) {
            return repo.findByCodeContainingIgnoreCase(safeCode, pageable);
        }

        return repo.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public java.util.Optional<Payor> findOne(Long id) {
        return repo.findById(id);
    }

    public java.util.Optional<Payor> toggleIsActive(Long id) {
        return repo.findById(id)
                .map(p -> {
                    p.setIsActive(!Boolean.TRUE.equals(p.getIsActive()));
                    return repo.save(p);
                });
    }

    @Transactional(readOnly = true)
    public Page<Payor> getAllActive(Pageable pageable) {
        return repo.findByIsActiveTrue(pageable);
    }

    @Transactional
    public Payor upsertCchiPayor(CchiPayorUpsertVM vm) {
        String payerNphiesId = clean(vm.payerNphiesId());
        String payerId = clean(vm.payerId());
        String payerName = clean(vm.payerName());

        String code = firstNonBlank(payerNphiesId, payerId);

        if (isBlank(code)) {
            throw new IllegalArgumentException("Missing payerNphiesId/payerId from Waseel");
        }

        Payor payor = null;

        if (!isBlank(payerNphiesId)) {
            payor = repo.findFirstByNphiesId(payerNphiesId).orElse(null);
        }

        if (payor == null) {
            payor = repo.findFirstByCode(truncate(code, 50)).orElseGet(Payor::new);
        }

        payor.setCode(truncate(code, 50));
        payor.setName(firstNonBlank(payerName, code));

        payor.setNphiesId(payerNphiesId);
        payor.setWaseelPayerId(payerId);
        payor.setTpaNphiesId(clean(vm.tpaNphiesId()));

        payor.setIsWaseelEnabled(true);
        payor.setIsActive(true);

        if (payor.getCategory() == null) {
            payor.setCategory(PayorCategory.INSURANCE);
        }

        if (payor.getRenewable() == null) {
            payor.setRenewable(false);
        }

        if (payor.getAllowPartialCoverage() == null) {
            payor.setAllowPartialCoverage(false);
        }

        if (payor.getAcceptCopay() == null) {
            payor.setAcceptCopay(false);
        }

        if (payor.getAcceptDeductibles() == null) {
            payor.setAcceptDeductibles(false);
        }

        if (payor.getAllowPackagePricing() == null) {
            payor.setAllowPackagePricing(false);
        }

        if (payor.getAllowDrgBilling() == null) {
            payor.setAllowDrgBilling(false);
        }

        if (payor.getForcePreApproval() == null) {
            payor.setForcePreApproval(false);
        }

        return repo.save(payor);
    }

}

