package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.NphiesPayer;
import com.dazzle.asklepios.repository.NphiesPayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NphiesPayerService {

    private static final Logger LOG =
            LoggerFactory.getLogger(NphiesPayerService.class);

    private final NphiesPayerRepository nphiesPayerRepository;

    public NphiesPayerService(
            NphiesPayerRepository nphiesPayerRepository
    ) {
        this.nphiesPayerRepository = nphiesPayerRepository;
    }

    @Transactional(readOnly = true)
    public Page<NphiesPayer> findAll(Pageable pageable) {
        LOG.debug("[FIND ALL NPHIES PAYERS] Fetching all NPHIES payers pageable={}", pageable);

        Page<NphiesPayer> payersPage = nphiesPayerRepository.findAll(pageable);

        LOG.debug(
                "[FIND ALL NPHIES PAYERS] Retrieved count={} pageNumber={} pageSize={} totalElements={} totalPages={}",
                payersPage.getNumberOfElements(),
                payersPage.getNumber(),
                payersPage.getSize(),
                payersPage.getTotalElements(),
                payersPage.getTotalPages()
        );

        return payersPage;
    }

    @Transactional(readOnly = true)
    public Page<NphiesPayer> findByNphiesId(String nphiesId, Pageable pageable) {
        LOG.debug(
                "[FIND BY NPHIES ID] Searching NPHIES payers by nphiesId='{}' pageable={}",
                nphiesId,
                pageable
        );

        return nphiesPayerRepository.findByNphiesIdContainingIgnoreCase(
                nphiesId,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public Page<NphiesPayer> findByNameEn(String nameEn, Pageable pageable) {
        LOG.debug(
                "[FIND BY NAME EN] Searching NPHIES payers by nameEn='{}' pageable={}",
                nameEn,
                pageable
        );

        return nphiesPayerRepository.findByNameEnContainingIgnoreCase(
                nameEn,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public Page<NphiesPayer> findByNameAr(String nameAr, Pageable pageable) {
        LOG.debug(
                "[FIND BY NAME AR] Searching NPHIES payers by nameAr='{}' pageable={}",
                nameAr,
                pageable
        );

        return nphiesPayerRepository.findByNameArContainingIgnoreCase(
                nameAr,
                pageable
        );
    }
}