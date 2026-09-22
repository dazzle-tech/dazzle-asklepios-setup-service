package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.CoverageClass;
import com.dazzle.asklepios.domain.CoverageContract;
import com.dazzle.asklepios.repository.CoverageClassRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageClassResponseVM;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageClassSaveVM;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageClassUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CoverageClassService {

    private static final Logger LOG = LoggerFactory.getLogger(CoverageClassService.class);
    private static final String ENTITY = "coverageClass";

    private final CoverageClassRepository coverageClassRepository;
    private final CoverageContractService coverageContractService;

    public CoverageClassService(
            CoverageClassRepository coverageClassRepository,
            CoverageContractService coverageContractService
    ) {
        this.coverageClassRepository = coverageClassRepository;
        this.coverageContractService = coverageContractService;
    }

    public CoverageClassResponseVM create(CoverageClassSaveVM vm) {
        LOG.debug("Create coverage class payload={}", vm);
        CoverageContract contract = coverageContractService.getEntity(vm.coverageContractId());
        String name = requireName(vm.name());
        ensureUniqueName(contract.getId(), name, null);

        CoverageClass saved = coverageClassRepository.save(CoverageClass.builder()
                .coverageContract(contract)
                .name(name)
                .isActive(vm.isActive() != null ? vm.isActive() : Boolean.TRUE)
                .build());
        return CoverageClassResponseVM.of(saved);
    }

    public CoverageClassResponseVM update(CoverageClassUpdateVM vm) {
        LOG.debug("Update coverage class payload={}", vm);
        CoverageClass existing = getEntity(vm.id());
        String name = requireName(vm.name());
        ensureUniqueName(existing.getCoverageContract().getId(), name, existing.getId());
        existing.setName(name);
        if (vm.isActive() != null) {
            existing.setIsActive(vm.isActive());
        }
        return CoverageClassResponseVM.of(coverageClassRepository.save(existing));
    }

    public CoverageClassResponseVM toggleActive(Long id) {
        CoverageClass existing = getEntity(id);
        existing.setIsActive(!Boolean.TRUE.equals(existing.getIsActive()));
        return CoverageClassResponseVM.of(coverageClassRepository.save(existing));
    }

    @Transactional(readOnly = true)
    public CoverageClassResponseVM get(Long id) {
        return CoverageClassResponseVM.of(getEntity(id));
    }

    @Transactional(readOnly = true)
    public CoverageClass getEntity(Long id) {
        return coverageClassRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException("Coverage class was not found.", ENTITY, "notFound"));
    }

    @Transactional(readOnly = true)
    public Page<CoverageClassResponseVM> list(Long contractId, Boolean isActive, Pageable pageable) {
        coverageContractService.getEntity(contractId);
        Page<CoverageClass> page = isActive == null
                ? coverageClassRepository.findByCoverageContract_Id(contractId, pageable)
                : coverageClassRepository.findByCoverageContract_IdAndIsActive(contractId, isActive, pageable);
        return page.map(CoverageClassResponseVM::of);
    }

    private String requireName(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BadRequestAlertException("Class name is required.", ENTITY, "nameRequired");
        }
        return raw.trim();
    }

    private void ensureUniqueName(Long contractId, String name, Long id) {
        boolean exists = id == null
                ? coverageClassRepository.existsByCoverageContract_IdAndNameIgnoreCase(contractId, name)
                : coverageClassRepository.existsByCoverageContract_IdAndNameIgnoreCaseAndIdNot(contractId, name, id);
        if (exists) {
            throw new BadRequestAlertException("Class name already exists under this coverage header.", ENTITY, "nameExists");
        }
    }
}
