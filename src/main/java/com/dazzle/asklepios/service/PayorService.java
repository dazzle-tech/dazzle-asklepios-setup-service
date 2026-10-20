package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.NphiesPayer;
import com.dazzle.asklepios.domain.Payor;
import com.dazzle.asklepios.domain.enumeration.biling.PayorCategory;
import com.dazzle.asklepios.repository.NphiesPayerRepository;
import com.dazzle.asklepios.repository.PayorRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.payor.PayorSaveVM;
import com.dazzle.asklepios.web.rest.vm.payor.PayorUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.firstNonBlank;

@Service
@Transactional
public class PayorService {

    private static final Logger LOG = LoggerFactory.getLogger(PayorService.class);

    private final PayorRepository repo;
    private final NphiesPayerRepository nphiesPayerRepository;

    public PayorService(PayorRepository repo, NphiesPayerRepository nphiesPayerRepository) {
        this.repo = repo;
        this.nphiesPayerRepository = nphiesPayerRepository;
    }

    // ------------ CREATE ------------
    public Payor create(PayorSaveVM vm) {
        LOG.debug("Create Payor payload={}", vm);

        if (vm.category() == PayorCategory.INSURANCE) {
            return linkInsuranceCompany(vm);
        }

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
                .phone(requiredText(vm.phone()))
                .email(vm.email())
                .contractManagerContact(requiredText(vm.contractManagerContact()))

                .startDate(vm.startDate() != null ? vm.startDate() : LocalDate.now())
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
        applyInsuranceCompanyIdentity(existing);

        return repo.save(existing);
    }

    /**
     * Keeps the operational payor row aligned with the insurance company.
     * Plans and patient insurance still store this row id.
     */
    public Payor syncFromInsuranceCompany(NphiesPayer payer) {
        return syncFromInsuranceCompany(payer, null);
    }

    public Payor syncFromInsuranceCompany(NphiesPayer payer, String previousNphiesId) {
        if (payer == null || payer.getNphiesId() == null || payer.getNphiesId().isBlank()) {
            throw new BadRequestAlertException("NPHIES ID is required.", "payor", "nphiesIdRequired");
        }

        Payor linked = findByNphiesId(payer.getNphiesId()).orElse(null);
        if (linked == null && previousNphiesId != null && !previousNphiesId.isBlank()) {
            linked = findByNphiesId(previousNphiesId).orElse(null);
        }
        if (linked == null) {
            linked = ensureFromNphiesId(payer.getNphiesId());
        }
        linked.setName(firstNonBlank(payer.getNameEn(), payer.getNameAr(), linked.getName()));
        linked.setNphiesId(payer.getNphiesId());
        linked.setCategory(PayorCategory.INSURANCE);
        linked.setPhone(requiredText(firstNonBlank(payer.getPhone(), payer.getMobile(), linked.getPhone())));
        linked.setEmail(firstNonBlank(payer.getEmail(), linked.getEmail()));
        linked.setAddress(firstNonBlank(payer.getHeadOfficeAddress(), linked.getAddress()));
        linked.setContractManagerContact(
                requiredText(firstNonBlank(payer.getContactPerson(), linked.getContractManagerContact()))
        );
        linked.setIsActive(payer.getIsActive() == null || Boolean.TRUE.equals(payer.getIsActive()));
        return repo.save(linked);
    }

    private Payor linkInsuranceCompany(PayorSaveVM vm) {
        String nphiesId = vm.nphiesId() == null ? null : vm.nphiesId().trim();
        if (nphiesId == null || nphiesId.isBlank()) {
            throw new BadRequestAlertException(
                    "Insurance companies are one record. Add the company under Insurance Companies.",
                    "payor",
                    "insuranceCompany.useMaster"
            );
        }

        NphiesPayer payer = nphiesPayerRepository.findFirstByNphiesIdIgnoreCase(nphiesId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "The insurance company was not found.",
                        "payor",
                        "nphiesPayerNotFound"
                ));

        Payor linked = syncFromInsuranceCompany(payer);
        validateDates(vm.startDate(), vm.expiryDate());
        if (vm.startDate() != null) {
            linked.setStartDate(vm.startDate());
        }
        linked.setExpiryDate(vm.expiryDate());
        if (vm.renewable() != null) {
            linked.setRenewable(vm.renewable());
        }
        if (vm.waseelPayerId() != null && !vm.waseelPayerId().isBlank()) {
            linked.setWaseelPayerId(vm.waseelPayerId());
        }
        if (vm.tpaNphiesId() != null) {
            linked.setTpaNphiesId(vm.tpaNphiesId());
        }
        if (vm.isWaseelEnabled() != null) {
            linked.setIsWaseelEnabled(vm.isWaseelEnabled());
        }
        linked.setAllowPartialCoverage(bool(vm.allowPartialCoverage()));
        linked.setAcceptCopay(bool(vm.acceptCopay()));
        linked.setAcceptDeductibles(bool(vm.acceptDeductibles()));
        linked.setAllowPackagePricing(bool(vm.allowPackagePricing()));
        linked.setAllowDrgBilling(bool(vm.allowDrgBilling()));
        linked.setForcePreApproval(bool(vm.forcePreApproval()));
        return repo.save(linked);
    }

    private void applyInsuranceCompanyIdentity(Payor payor) {
        if (payor.getCategory() != PayorCategory.INSURANCE
                || payor.getNphiesId() == null
                || payor.getNphiesId().isBlank()) {
            return;
        }

        nphiesPayerRepository.findFirstByNphiesIdIgnoreCase(payor.getNphiesId().trim())
                .ifPresent(payer -> {
                    payor.setName(firstNonBlank(payer.getNameEn(), payer.getNameAr(), payor.getName()));
                    payor.setNphiesId(payer.getNphiesId());
                    payor.setIsActive(payer.getIsActive() == null || Boolean.TRUE.equals(payer.getIsActive()));
                });
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

    @Transactional(readOnly = true)
    public Optional<Payor> findByNphiesId(String nphiesId) {
        if (nphiesId == null || nphiesId.trim().isEmpty()) {
            return Optional.empty();
        }

        String trimmed = nphiesId.trim();
        return repo.findFirstByNphiesIdIgnoreCase(trimmed)
                .or(() -> repo.findFirstByNphiesId(trimmed));
    }

    public Payor ensureFromNphiesId(String nphiesId) {
        if (nphiesId == null || nphiesId.isBlank()) {
            throw new BadRequestAlertException("NPHIES ID is required.", "payor", "nphiesIdRequired");
        }

        String trimmed = nphiesId.trim();
        Optional<Payor> existing = findByNphiesId(trimmed);
        if (existing.isPresent()) {
            return existing.get();
        }

        NphiesPayer payer = nphiesPayerRepository.findFirstByNphiesIdIgnoreCase(trimmed)
                .orElseThrow(() -> new BadRequestAlertException(
                        "NPHIES payer was not found for id " + trimmed + ".",
                        "payor",
                        "nphiesPayerNotFound"
                ));

        String name = firstNonBlank(payer.getNameEn(), payer.getNameAr(), payer.getNphiesId(), trimmed);
        Payor created = Payor.builder()
                .code(uniquePayorCode(payer.getNphiesId() == null ? trimmed : payer.getNphiesId()))
                .name(name)
                .category(PayorCategory.INSURANCE)
                .nphiesId(payer.getNphiesId())
                .waseelPayerId(payer.getNphiesId())
                .phone(requiredText(firstNonBlank(payer.getPhone(), payer.getMobile())))
                .contractManagerContact(requiredText(payer.getContactPerson()))
                .startDate(LocalDate.now())
                .address(payer.getHeadOfficeAddress())
                .email(payer.getEmail())
                .renewable(false)
                .allowPartialCoverage(false)
                .acceptCopay(false)
                .acceptDeductibles(false)
                .allowPackagePricing(false)
                .allowDrgBilling(false)
                .forcePreApproval(false)
                .isWaseelEnabled(false)
                .isActive(true)
                .build();

        LOG.info("Created Payor from NPHIES payer nphiesId={} name={}", created.getNphiesId(), created.getName());
        return repo.save(created);
    }

    private static String requiredText(String value) {
        return firstNonBlank(value, "N/A");
    }

    private String uniquePayorCode(String nphiesId) {
        String sanitized = nphiesId == null ? "" : nphiesId.replaceAll("[^A-Za-z0-9_-]", "").toUpperCase(Locale.ROOT);
        String base = sanitized.isBlank() ? "NPH" : ("NPH-" + sanitized);
        if (base.length() > 40) {
            base = base.substring(0, 40);
        }
        if (!repo.existsByCodeIgnoreCase(base)) {
            return base;
        }
        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        String withSuffix = base + "-" + suffix;
        return withSuffix.length() > 50 ? withSuffix.substring(0, 50) : withSuffix;
    }

}

