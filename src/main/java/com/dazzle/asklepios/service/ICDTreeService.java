package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.ICDCategory;
import com.dazzle.asklepios.domain.ICDDiagnosis;
import com.dazzle.asklepios.repository.ICDCategoryRepository;
import com.dazzle.asklepios.repository.ICDDiagnosisRepository;
import com.dazzle.asklepios.service.dto.icd10.ICDCategoryDTO;
import com.dazzle.asklepios.service.dto.icd10.ICDDiagnosisDTO;
import com.dazzle.asklepios.service.dto.icd10.ICDNodeDetailsDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ICDTreeService {

    private static final Logger LOG = LoggerFactory.getLogger(ICDTreeService.class);

    private final ICDCategoryRepository icdCategoryRepository;
    private final ICDDiagnosisRepository icdDiagnosisRepository;

    @Transactional(readOnly = true)
    public Page<ICDCategoryDTO> getRootCategories(String icdCoding, Pageable pageable) {

        return icdCategoryRepository
                .findByIcdCodingAndParentCategoryCodeIsNull(icdCoding, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ICDCategoryDTO> getChildren(String icdCoding, String parentCategoryCode, Pageable pageable) {


        return icdCategoryRepository
                .findByIcdCodingAndParentCategoryCode(icdCoding, parentCategoryCode, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ICDDiagnosisDTO> getDiagnosesByCategory(String icdCoding, String categoryCode, Pageable pageable) {


        return icdDiagnosisRepository
                .findByIcdCodingAndCategoryCode(icdCoding, categoryCode, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public ICDNodeDetailsDTO getNodeDetails(String icdCoding, String categoryCode, Pageable pageable) {


        ICDCategory selected = icdCategoryRepository.findById(categoryCode)
                .orElseThrow(() -> new NotFoundAlertException(
                        "ICD category not found with code " + categoryCode,
                        "icdTree",
                        "category.notfound"
                ));

        if (!icdCoding.equalsIgnoreCase(selected.getIcdCoding())) {
            throw new BadRequestAlertException(
                    "Category does not belong to icdCoding=" + icdCoding,
                    "icdTree",
                    "category.coding.mismatch"
            );
        }

        Page<ICDCategoryDTO> childrenPage =
                icdCategoryRepository
                        .findByIcdCodingAndParentCategoryCode(icdCoding, categoryCode, pageable)
                        .map(this::toDTO);

        Page<ICDDiagnosisDTO> diagnosesPage =
                icdDiagnosisRepository
                        .findByIcdCodingAndCategoryCode(icdCoding, categoryCode, pageable)
                        .map(this::toDTO);

        return new ICDNodeDetailsDTO(
                toDTO(selected),
                childrenPage.getContent(),
                diagnosesPage.getContent()
        );
    }

    private ICDCategoryDTO toDTO(ICDCategory e) {
        return new ICDCategoryDTO(
                e.getCategoryCode(),
                e.getIcdCoding(),
                e.getCategoryName(),
                e.getCategoryDescription(),
                e.getParentCategoryCode()
        );
    }

    private ICDDiagnosisDTO toDTO(ICDDiagnosis e) {
        return new ICDDiagnosisDTO(
                e.getId(),
                e.getIcdDiagnosisUid(),
                e.getIcdCode(),
                e.getIcdCoding(),
                e.getCategoryCode(),
                e.getIcdShortDescription(),
                e.getIcdFullDescription()
        );
    }
}
