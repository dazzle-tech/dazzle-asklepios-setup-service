package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.NphiesPayer;
import com.dazzle.asklepios.domain.enumeration.InsuranceCompanySource;
import com.dazzle.asklepios.repository.NphiesPayerRepository;
import com.dazzle.asklepios.service.dto.InsuranceCompanyCreateVM;
import com.dazzle.asklepios.service.dto.InsuranceCompanyDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class InsuranceCompanyService {

    private static final String ENTITY = "insuranceCompany";

    private final NphiesPayerRepository nphiesPayerRepository;

    private final PayorService payorService;

    @Transactional(readOnly = true)
    public Page<InsuranceCompanyDTO> search(
            InsuranceCompanySource source,
            String search,
            Pageable pageable
    ) {
        if (source != null) {
            return searchNphies(search, pageable);
        }
        return searchNphies(search, pageable);
    }

    public InsuranceCompanyDTO createInternal(InsuranceCompanyCreateVM vm) {
        String nphiesId = blankToNull(vm.nphiesId());
        String code = blankToNull(vm.code());
        NphiesPayer payer = null;
        if (nphiesId != null) {
            payer = nphiesPayerRepository.findFirstByNphiesIdIgnoreCase(nphiesId).orElse(null);
        }
        if (payer == null && code != null) {
            payer = nphiesPayerRepository.findFirstByNphiesIdIgnoreCase(code).orElse(null);
        }
        if (payer == null) {
            throw new BadRequestAlertException(
                    "Insurance companies are one record. Create the company under Insurance Companies.",
                    ENTITY,
                    "insuranceCompany.useMaster"
            );
        }

        payorService.syncFromInsuranceCompany(payer);
        return toNphiesDto(payer);
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

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
