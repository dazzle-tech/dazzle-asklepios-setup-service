package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.CoverageClass;
import com.dazzle.asklepios.domain.CoverageContract;
import com.dazzle.asklepios.domain.NphiesPayer;
import com.dazzle.asklepios.domain.Payor;
import com.dazzle.asklepios.domain.TpaDefinition;
import com.dazzle.asklepios.domain.enumeration.GuarantorType;
import com.dazzle.asklepios.repository.CoverageClassRepository;
import com.dazzle.asklepios.repository.NphiesPayerRepository;
import com.dazzle.asklepios.repository.PayorRepository;
import com.dazzle.asklepios.repository.TpaDefinitionRepository;
import com.dazzle.asklepios.web.rest.vm.tpadefinition.TpaResolutionVM;
import com.dazzle.asklepios.web.rest.vm.tpadefinition.TpaResolutionVM.Candidate;
import com.dazzle.asklepios.web.rest.vm.tpadefinition.TpaResolutionVM.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Decides which TPA, if any, handles one insurance policy. A payer can work with several TPAs,
 * so the answer belongs to the policy and cannot be read from the payer alone.
 */
@Service
@Transactional(readOnly = true)
public class TpaResolutionService {

    /** Waseel placeholder for "no TPA". */
    private static final String NO_TPA_MARKER = "-1";

    private final TpaDefinitionRepository tpaDefinitionRepository;
    private final NphiesPayerRepository nphiesPayerRepository;
    private final PayorRepository payorRepository;
    private final CoverageClassRepository coverageClassRepository;

    public TpaResolutionService(
            TpaDefinitionRepository tpaDefinitionRepository,
            NphiesPayerRepository nphiesPayerRepository,
            PayorRepository payorRepository,
            CoverageClassRepository coverageClassRepository
    ) {
        this.tpaDefinitionRepository = tpaDefinitionRepository;
        this.nphiesPayerRepository = nphiesPayerRepository;
        this.payorRepository = payorRepository;
        this.coverageClassRepository = coverageClassRepository;
    }

    /**
     * @param preferredTpaNphiesId a TPA already known for this policy, from CCHI or chosen by the
     *                             user; it wins over anything derived from setup.
     */
    public TpaResolutionVM resolve(String payerNphiesId, String policyNumber, String preferredTpaNphiesId) {
        String payerId = clean(payerNphiesId);
        NphiesPayer payer = payerId == null
                ? null
                : nphiesPayerRepository.findFirstByNphiesIdIgnoreCase(payerId).orElse(null);
        List<TpaDefinition> linked = linkedActiveTpas(payer);
        List<Candidate> candidates = linked.stream()
                .map(tpa -> new Candidate(tpa.getId(), tpa.getName(), clean(tpa.getNphiesId())))
                .toList();

        String preferred = normalizeTpaId(preferredTpaNphiesId);
        if (preferred != null) {
            return resolved(preferred, candidates);
        }

        String payorDefault = payerId == null ? null : payorDefaultTpa(payerId);
        if (linked.isEmpty()) {
            return payorDefault == null
                    ? new TpaResolutionVM(Status.NO_TPA, null, null, candidates)
                    : resolved(payorDefault, candidates);
        }
        if (linked.size() == 1) {
            return fromTpa(linked.get(0), payorDefault, candidates);
        }

        List<TpaDefinition> contracted = contractedTpas(payer, policyNumber, linked);
        if (contracted.size() == 1) {
            return fromTpa(contracted.get(0), null, candidates);
        }
        return new TpaResolutionVM(Status.SELECTION_REQUIRED, null, null, candidates);
    }

    private List<TpaDefinition> linkedActiveTpas(NphiesPayer payer) {
        if (payer == null) {
            return List.of();
        }
        return tpaDefinitionRepository.findByInsuranceCompanies_Id(payer.getId()).stream()
                .filter(tpa -> Boolean.TRUE.equals(tpa.getIsActive()))
                .sorted(Comparator.comparing(TpaDefinition::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    private List<TpaDefinition> contractedTpas(NphiesPayer payer, String policyNumber, List<TpaDefinition> linked) {
        String policy = clean(policyNumber);
        if (policy == null) {
            return List.of();
        }

        Map<Long, TpaDefinition> linkedById = linked.stream()
                .collect(Collectors.toMap(TpaDefinition::getId, Function.identity()));
        Set<Long> contractedIds = coverageClassRepository
                .findByIsActiveTrueAndCoverageContract_IsActiveTrueAndCoverageContract_InsurancePayerIdAndInsuranceCompanyPolicy_PolicyNumberIgnoreCase(
                        payer.getId(),
                        policy
                )
                .stream()
                .map(CoverageClass::getCoverageContract)
                .filter(contract -> contract != null && contract.getGuarantorType() == GuarantorType.TPA)
                .map(CoverageContract::getCompanyId)
                .filter(linkedById::containsKey)
                .collect(Collectors.toSet());

        return contractedIds.stream().map(linkedById::get).toList();
    }

    private String payorDefaultTpa(String payerNphiesId) {
        return payorRepository.findFirstByNphiesIdIgnoreCase(payerNphiesId)
                .map(Payor::getTpaNphiesId)
                .map(TpaResolutionService::normalizeTpaId)
                .orElse(null);
    }

    private TpaResolutionVM fromTpa(TpaDefinition tpa, String fallbackNphiesId, List<Candidate> candidates) {
        String nphiesId = Optional.ofNullable(normalizeTpaId(tpa.getNphiesId())).orElse(fallbackNphiesId);
        return nphiesId == null
                ? new TpaResolutionVM(Status.NPHIES_ID_MISSING, null, tpa.getName(), candidates)
                : new TpaResolutionVM(Status.RESOLVED, nphiesId, tpa.getName(), candidates);
    }

    private TpaResolutionVM resolved(String nphiesId, List<Candidate> candidates) {
        String name = tpaDefinitionRepository.findFirstByNphiesIdIgnoreCase(nphiesId)
                .map(TpaDefinition::getName)
                .orElse(null);
        return new TpaResolutionVM(Status.RESOLVED, nphiesId, name, candidates);
    }

    private static String normalizeTpaId(String value) {
        String id = clean(value);
        return NO_TPA_MARKER.equals(id) ? null : id;
    }

    private static String clean(String value) {
        if (value == null) {
            return null;
        }
        String text = value.trim();
        return text.isEmpty() ? null : text;
    }
}
