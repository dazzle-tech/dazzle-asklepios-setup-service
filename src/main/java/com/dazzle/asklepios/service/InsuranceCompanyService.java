package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.NphiesPayer;
import com.dazzle.asklepios.domain.Payor;
import com.dazzle.asklepios.domain.enumeration.InsuranceCompanySource;
import com.dazzle.asklepios.domain.enumeration.biling.PayorCategory;
import com.dazzle.asklepios.repository.NphiesPayerRepository;
import com.dazzle.asklepios.repository.PayorRepository;
import com.dazzle.asklepios.service.dto.InsuranceCompanyCreateVM;
import com.dazzle.asklepios.service.dto.InsuranceCompanyDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class InsuranceCompanyService {

    private static final String ENTITY = "insuranceCompany";

    private final NphiesPayerRepository nphiesPayerRepository;

    private final PayorRepository payorRepository;

    @Transactional(readOnly = true)
    public Page<InsuranceCompanyDTO> search(
            InsuranceCompanySource source,
            String search,
            Pageable pageable
    ) {
        if (source == InsuranceCompanySource.NPHIES) {
            return searchNphies(search, pageable);
        }

        if (source == InsuranceCompanySource.PAYOR) {
            return searchPayors(search, pageable);
        }

        Page<InsuranceCompanyDTO> nphies = searchNphies(search, pageable);
        Page<InsuranceCompanyDTO> payors = searchPayors(search, pageable);

        List<InsuranceCompanyDTO> combined = new ArrayList<>();
        combined.addAll(nphies.getContent());
        combined.addAll(payors.getContent());

        return new PageImpl<>(
                combined,
                pageable,
                nphies.getTotalElements() + payors.getTotalElements()
        );
    }

    public InsuranceCompanyDTO createInternal(InsuranceCompanyCreateVM vm) {
        String name = vm.name().trim();
        String code = vm.code() != null && !vm.code().isBlank()
                ? vm.code().trim()
                : generateCode(name);

        if (payorRepository.existsByCodeIgnoreCase(code)) {
            throw new BadRequestAlertException(
                    "An insurance company with this code already exists.",
                    ENTITY,
                    "code.exists"
            );
        }

        Payor payor = Payor.builder()
                .code(code)
                .name(name)
                .category(PayorCategory.INSURANCE)
                .nphiesId(blankToNull(vm.nphiesId()))
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

        return toPayorDto(payorRepository.save(payor));
    }

    private Page<InsuranceCompanyDTO> searchNphies(
            String search,
            Pageable pageable
    ) {
        Page<NphiesPayer> page;
        if (search == null || search.isBlank()) {
            page = nphiesPayerRepository.findByIsActiveTrue(pageable);
        } else {
            page = nphiesPayerRepository.findByIsActiveTrueAndNameEnContainingIgnoreCase(
                    search.trim(),
                    pageable
            );
        }

        return page.map(this::toNphiesDto);
    }

    private Page<InsuranceCompanyDTO> searchPayors(
            String search,
            Pageable pageable
    ) {
        Page<Payor> page;
        if (search == null || search.isBlank()) {
            page = payorRepository.findByIsActiveTrueAndCategory(
                    PayorCategory.INSURANCE,
                    pageable
            );
        } else {
            page = payorRepository.findByIsActiveTrueAndCategoryAndNameContainingIgnoreCase(
                    PayorCategory.INSURANCE,
                    search.trim(),
                    pageable
            );
        }

        return page.map(this::toPayorDto);
    }

    private InsuranceCompanyDTO toNphiesDto(NphiesPayer payer) {
        return new InsuranceCompanyDTO(
                payer.getId(),
                InsuranceCompanySource.NPHIES,
                null,
                payer.getId(),
                payer.getNphiesId(),
                payer.getNameEn(),
                payer.getNameAr(),
                payer.getNphiesId(),
                payer.getIsActive()
        );
    }

    private InsuranceCompanyDTO toPayorDto(Payor payor) {
        return new InsuranceCompanyDTO(
                payor.getId(),
                InsuranceCompanySource.PAYOR,
                payor.getId(),
                null,
                payor.getCode(),
                payor.getName(),
                null,
                payor.getNphiesId(),
                payor.getIsActive()
        );
    }

    private String generateCode(String name) {
        String prefix = name.replaceAll("[^A-Za-z0-9]", "")
                .toUpperCase(Locale.ROOT);
        if (prefix.length() > 8) {
            prefix = prefix.substring(0, 8);
        }
        if (prefix.isBlank()) {
            prefix = "INS";
        }
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
