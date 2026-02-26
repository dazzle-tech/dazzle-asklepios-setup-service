package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.ICDCategory;
import com.dazzle.asklepios.domain.ICDDiagnosis;
import com.dazzle.asklepios.repository.ICDCategoryRepository;
import com.dazzle.asklepios.repository.ICDDiagnosisRepository;
import com.dazzle.asklepios.service.dto.icd10.ICDNodeDetailsEntityDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ICDTreeService {

    private static final Logger LOG = LoggerFactory.getLogger(ICDTreeService.class);

    private final ICDCategoryRepository icdCategoryRepository;
    private final ICDDiagnosisRepository icdDiagnosisRepository;

    @Transactional(readOnly = true)
    public Page<ICDCategory> getRootCategories(String icdCoding, Pageable pageable) {
        LOG.debug("[GET ROOT CATEGORIES] icdCoding='{}' pageable={}", icdCoding, pageable);
        return icdCategoryRepository.findByIcdCodingAndParentCategoryIsNull(icdCoding, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ICDCategory> getChildren(String icdCoding, String parentCategoryCode, Pageable pageable) {
        LOG.debug(
                "[GET CHILD CATEGORIES] icdCoding='{}' parentCategoryCode='{}' pageable={}",
                icdCoding, parentCategoryCode, pageable
        );

        return icdCategoryRepository.findByIcdCodingAndParentCategory_CategoryCode(
                icdCoding,
                parentCategoryCode,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public Page<ICDDiagnosis> getDiagnosesByCategory(String icdCoding, String categoryCode, Pageable pageable) {
        LOG.debug(
                "[GET DIAGNOSES BY CATEGORY] icdCoding='{}' categoryCode='{}' pageable={}",
                icdCoding, categoryCode, pageable
        );

        return icdDiagnosisRepository.findByIcdCodingAndCategoryCode(icdCoding, categoryCode, pageable);
    }

    @Transactional(readOnly = true)
    public ICDNodeDetailsEntityDTO getNodeDetails(String icdCoding, String categoryCode, Pageable pageable) {
        LOG.debug(
                "[GET NODE DETAILS] icdCoding='{}' categoryCode='{}' pageable={}",
                icdCoding, categoryCode, pageable
        );

        ICDCategory selected = icdCategoryRepository.findById(categoryCode)
                .orElseThrow(() -> {
                    LOG.warn("[GET NODE DETAILS] Category not found categoryCode='{}'", categoryCode);
                    return new NotFoundAlertException(
                            "ICD category not found with code " + categoryCode,
                            "icdTree",
                            "category.notfound"
                    );
                });

        if (!icdCoding.equalsIgnoreCase(selected.getIcdCoding())) {
            LOG.warn(
                    "[GET NODE DETAILS] Coding mismatch categoryCode='{}' expected='{}' actual='{}'",
                    categoryCode, icdCoding, selected.getIcdCoding()
            );
            throw new BadRequestAlertException(
                    "Category does not belong to icdCoding=" + icdCoding,
                    "icdTree",
                    "category.coding.mismatch"
            );
        }

        Page<ICDCategory> childrenPage =
                icdCategoryRepository.findByIcdCodingAndParentCategory_CategoryCode(icdCoding, categoryCode, pageable);

        Page<ICDDiagnosis> diagnosesPage =
                icdDiagnosisRepository.findByIcdCodingAndCategoryCode(icdCoding, categoryCode, pageable);

        LOG.debug(
                "[GET NODE DETAILS] childrenCount={} diagnosesCount={}",
                childrenPage.getTotalElements(),
                diagnosesPage.getTotalElements()
        );

        return new ICDNodeDetailsEntityDTO(
                selected,
                childrenPage.getContent(),
                diagnosesPage.getContent()
        );
    }

    @Transactional(readOnly = true)
    public Page<ICDDiagnosis> searchDiagnoses(String keyword, Pageable pageable) {
        LOG.debug("[FIND DIAGNOSES BY KEYWORD] keyword='{}' pageable={}", keyword, pageable);

        Page<ICDDiagnosis> page =
                icdDiagnosisRepository
                        .findByIcdCodeContainingIgnoreCaseOrIcdShortDescriptionContainingIgnoreCaseOrIcdFullDescriptionContainingIgnoreCase(
                                keyword,
                                keyword,
                                keyword,
                                pageable
                        );

        LOG.debug("[FIND DIAGNOSES BY KEYWORD] resultCount={}", page.getTotalElements());
        return page;
    }

    @Transactional(readOnly = true)
    public List<ICDDiagnosis> findByIds(List<Long> ids) {
        LOG.debug("[GET DIAGNOSES BY IDS] ids={}", ids);
        return icdDiagnosisRepository.findAllById(ids);
    }

    @Transactional(readOnly = true)
    public ICDDiagnosis getDiagnosisById(Long id) {
        LOG.debug("[GET DIAGNOSIS BY ID] id={}", id);

        return icdDiagnosisRepository.findById(id)
                .orElseThrow(() -> {
                    LOG.warn("[GET DIAGNOSIS BY ID] Diagnosis not found id={}", id);
                    return new NotFoundAlertException(
                            "ICD diagnosis not found with id " + id,
                            "icdTree",
                            "diagnosis.notfound"
                    );
                });
    }
}
