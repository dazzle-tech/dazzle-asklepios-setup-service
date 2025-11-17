package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.CdtCode;
import com.dazzle.asklepios.domain.CdtDentalAction;
import com.dazzle.asklepios.domain.DentalAction;
import com.dazzle.asklepios.repository.CdtCodeRepository;
import com.dazzle.asklepios.repository.CdtDentalActionRepository;
import com.dazzle.asklepios.repository.DentalActionRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CdtDentalActionService {

    private static final Logger LOG = LoggerFactory.getLogger(CdtDentalActionService.class);

    private final CdtDentalActionRepository repository;
    private final DentalActionRepository dentalActionRepository;
    private final CdtCodeRepository cdtCodeRepository;

    public CdtDentalAction create(Long dentalActionId, Long cdtIdStr) {

        LOG.debug("Create CdtDentalAction: dentalActionId={}, cdtId={}", dentalActionId, cdtIdStr);

        // Load existing dentalAction
        DentalAction dentalAction = dentalActionRepository.findById(dentalActionId)
                .orElseThrow(() ->
        new BadRequestAlertException(
                "Invalid dentalActionId: " + dentalActionId,
                "cdtdentalaction",
                "Invalid dentalActionId: " + dentalActionId
        ));

        // Load existing CDT code
        CdtCode cdtCode = cdtCodeRepository.findById(cdtIdStr)
                .orElseThrow(() ->
                       new BadRequestAlertException(
                                "notExist" ,
                                "cdtdentalaction",
                               " CDT Code does not exist: " + cdtIdStr
                        ) );

        // Build entity
        CdtDentalAction entity = new CdtDentalAction();
        entity.setDentalAction(dentalAction);
        entity.setCdtId(cdtCode);

        // Save link properly
        return repository.save(entity);
    }

    public List<CdtDentalAction> findByDentalActionId(Long dentalActionId) {
        return repository.findByDentalAction_Id(dentalActionId);
    }

    public List<CdtDentalAction> findByCdtCode(Long cdtId) {
        return repository.findByCdtId_Id(cdtId);
    }

    public void delete(Long id) {
        LOG.debug("Delete CdtDentalAction id={}", id);
        repository.deleteById(id);
    }
}
