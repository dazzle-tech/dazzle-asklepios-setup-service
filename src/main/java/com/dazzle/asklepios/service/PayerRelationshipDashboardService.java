package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.CoverageClass;
import com.dazzle.asklepios.domain.CoverageContract;
import com.dazzle.asklepios.domain.NphiesPayer;
import com.dazzle.asklepios.domain.PriceListSetup;
import com.dazzle.asklepios.domain.TpaDefinition;
import com.dazzle.asklepios.domain.enumeration.GuarantorType;
import com.dazzle.asklepios.repository.CoverageClassRepository;
import com.dazzle.asklepios.repository.CoverageContractRepository;
import com.dazzle.asklepios.repository.NphiesPayerRepository;
import com.dazzle.asklepios.repository.PriceListSetupRepository;
import com.dazzle.asklepios.repository.TpaDefinitionRepository;
import com.dazzle.asklepios.web.rest.vm.nphiespayer.PayerRelationshipDashboardVM;
import com.dazzle.asklepios.web.rest.vm.nphiespayer.PayerRelationshipDashboardVM.ContractItemVM;
import com.dazzle.asklepios.web.rest.vm.nphiespayer.PayerRelationshipDashboardVM.InsuranceCardVM;
import com.dazzle.asklepios.web.rest.vm.nphiespayer.PayerRelationshipDashboardVM.InsuranceListItemVM;
import com.dazzle.asklepios.web.rest.vm.nphiespayer.PayerRelationshipDashboardVM.LinkedPartyVM;
import com.dazzle.asklepios.web.rest.vm.nphiespayer.PayerRelationshipDashboardVM.PriceListItemVM;
import com.dazzle.asklepios.web.rest.vm.nphiespayer.PayerRelationshipDashboardVM.SummaryVM;
import com.dazzle.asklepios.web.rest.vm.nphiespayer.PayerRelationshipDashboardVM.TpaCardVM;
import com.dazzle.asklepios.web.rest.vm.nphiespayer.PayerRelationshipDashboardVM.TpaListItemVM;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

@Service
@Transactional(readOnly = true)
public class PayerRelationshipDashboardService {

    private static final Comparator<NphiesPayer> PAYER_ORDER =
            Comparator.comparing(NphiesPayer::getNameEn, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
    private static final Comparator<TpaDefinition> TPA_ORDER =
            Comparator.comparing(TpaDefinition::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
    private static final Comparator<LinkedPartyVM> PARTY_ORDER =
            Comparator.comparing(LinkedPartyVM::name, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                    .thenComparing(LinkedPartyVM::code, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));

    private final NphiesPayerRepository nphiesPayerRepository;
    private final TpaDefinitionRepository tpaDefinitionRepository;
    private final CoverageClassRepository coverageClassRepository;
    private final CoverageContractRepository coverageContractRepository;
    private final PriceListSetupRepository priceListSetupRepository;

    public PayerRelationshipDashboardService(
            NphiesPayerRepository nphiesPayerRepository,
            TpaDefinitionRepository tpaDefinitionRepository,
            CoverageClassRepository coverageClassRepository,
            CoverageContractRepository coverageContractRepository,
            PriceListSetupRepository priceListSetupRepository
    ) {
        this.nphiesPayerRepository = nphiesPayerRepository;
        this.tpaDefinitionRepository = tpaDefinitionRepository;
        this.coverageClassRepository = coverageClassRepository;
        this.coverageContractRepository = coverageContractRepository;
        this.priceListSetupRepository = priceListSetupRepository;
    }

    public PayerRelationshipDashboardVM build(String search) {
        String query = search == null ? "" : search.trim();
        List<NphiesPayer> payers = loadPayers(query);
        List<TpaDefinition> tpas = loadTpas(query);
        SummaryVM summary = query.isEmpty() ? toSummary(payers, tpas) : null;
        return new PayerRelationshipDashboardVM(
                summary,
                payers.stream().map(this::toInsuranceListItem).toList(),
                tpas.stream().map(this::toTpaListItem).toList()
        );
    }

    public Optional<InsuranceCardVM> findInsurance(Long id) {
        return nphiesPayerRepository.findById(id).map(payer -> {
            List<TpaDefinition> linkedTpas = tpaDefinitionRepository.findByInsuranceCompanies_Id(id);
            List<PriceListSetup> priceLists = loadPriceLists(id);
            Map<Long, PriceListSetup> priceListsById = indexBy(priceLists, PriceListSetup::getId);
            Map<Long, NphiesPayer> payersById = indexBy(List.of(payer), NphiesPayer::getId);
            return toInsuranceCard(
                    payer,
                    linkedTpas,
                    priceLists,
                    coverageContractRepository.findByInsurancePayerId(id),
                    payersById,
                    priceListsById
            );
        });
    }

    public Optional<TpaCardVM> findTpa(Long id) {
        return tpaDefinitionRepository.findById(id).map(tpa -> {
            List<CoverageContract> contracts = coverageContractRepository.findByGuarantorTypeAndCompanyId(
                    GuarantorType.TPA,
                    id
            );
            return toTpaCard(tpa, contracts, lookupMaps(contracts));
        });
    }

    private List<NphiesPayer> loadPayers(String query) {
        List<NphiesPayer> payers = query.isEmpty()
                ? nphiesPayerRepository.findAll()
                : nphiesPayerRepository.findByNphiesIdContainingIgnoreCaseOrNameEnContainingIgnoreCaseOrNameArContainingIgnoreCase(
                        query,
                        query,
                        query
                );
        return payers.stream().sorted(PAYER_ORDER).toList();
    }

    private List<TpaDefinition> loadTpas(String query) {
        List<TpaDefinition> tpas = query.isEmpty()
                ? tpaDefinitionRepository.findAll()
                : tpaDefinitionRepository.findByTpaCodeContainingIgnoreCaseOrNameContainingIgnoreCase(query, query);
        return tpas.stream().sorted(TPA_ORDER).toList();
    }

    private InsuranceListItemVM toInsuranceListItem(NphiesPayer payer) {
        return new InsuranceListItemVM(
                payer.getId(),
                payer.getNphiesId(),
                payer.getNameEn(),
                payer.getNameAr(),
                payer.getIsActive()
        );
    }

    private TpaListItemVM toTpaListItem(TpaDefinition tpa) {
        return new TpaListItemVM(tpa.getId(), tpa.getTpaCode(), tpa.getName(), tpa.getIsActive());
    }

    private InsuranceCardVM toInsuranceCard(
            NphiesPayer payer,
            List<TpaDefinition> linkedTpas,
            List<PriceListSetup> priceLists,
            List<CoverageContract> contracts,
            Map<Long, NphiesPayer> payersById,
            Map<Long, PriceListSetup> priceListsById
    ) {
        return new InsuranceCardVM(
                payer.getId(),
                payer.getNphiesId(),
                payer.getNameEn(),
                payer.getNameAr(),
                payer.getIsActive(),
                payer.getApprovalCoverageCompany() == null ? null : payer.getApprovalCoverageCompany().name(),
                facilityName(payer),
                sortedParties(linkedTpas.stream().map(this::toParty).toList()),
                priceLists.stream()
                        .sorted(Comparator.comparing(PriceListSetup::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                        .map(this::toPriceListItem)
                        .toList(),
                toContractItems(contracts, payersById, priceListsById)
        );
    }

    private TpaCardVM toTpaCard(
            TpaDefinition tpa,
            List<CoverageContract> contracts,
            LookupMaps lookups
    ) {
        return new TpaCardVM(
                tpa.getId(),
                tpa.getTpaCode(),
                tpa.getName(),
                tpa.getIsActive(),
                tpa.getApprovalCoverageCompany() == null ? null : tpa.getApprovalCoverageCompany().name(),
                sortedParties(
                        tpa.getInsuranceCompanies() == null
                                ? List.of()
                                : tpa.getInsuranceCompanies().stream().map(this::toParty).toList()
                ),
                toContractItems(contracts, lookups.payersById, lookups.priceListsById)
        );
    }

    private SummaryVM toSummary(List<NphiesPayer> payers, List<TpaDefinition> tpas) {
        List<CoverageContract> contracts = coverageContractRepository.findAll();
        Map<Long, List<PriceListSetup>> priceListsByPayerId = groupPriceLists(priceListSetupRepository.findAll());
        Set<Long> insurancesWithContracts = new HashSet<>();
        Set<Long> tpasWithContracts = new HashSet<>();
        for (CoverageContract contract : contracts) {
            if (contract.getInsurancePayerId() != null) {
                insurancesWithContracts.add(contract.getInsurancePayerId());
            }
            if (contract.getGuarantorType() == GuarantorType.TPA && contract.getCompanyId() != null) {
                tpasWithContracts.add(contract.getCompanyId());
            }
        }
        int insurancesWithPriceLists = 0;
        for (NphiesPayer payer : payers) {
            if (payer.getId() != null && priceListsByPayerId.containsKey(payer.getId())) {
                insurancesWithPriceLists++;
            }
        }
        return new SummaryVM(
                payers.size(),
                tpas.size(),
                (int) payers.stream().filter(payer -> insurancesWithContracts.contains(payer.getId())).count(),
                insurancesWithPriceLists,
                (int) tpas.stream().filter(tpa -> tpasWithContracts.contains(tpa.getId())).count()
        );
    }

    private List<PriceListSetup> loadPriceLists(Long payerId) {
        Map<Long, PriceListSetup> unique = new LinkedHashMap<>();
        for (PriceListSetup list : priceListSetupRepository.findByNphiesPayerId(payerId)) {
            unique.put(list.getId(), list);
        }
        for (PriceListSetup list : priceListSetupRepository.findByPayerId(payerId)) {
            unique.putIfAbsent(list.getId(), list);
        }
        return new ArrayList<>(unique.values());
    }

    private LookupMaps lookupMaps(List<CoverageContract> contracts) {
        Set<Long> payerIds = new HashSet<>();
        Set<Long> priceListIds = new HashSet<>();
        for (CoverageContract contract : contracts) {
            if (contract.getInsurancePayerId() != null) {
                payerIds.add(contract.getInsurancePayerId());
            }
            if (contract.getParentPayerId() != null) {
                payerIds.add(contract.getParentPayerId());
            }
            if (contract.getPriceListSetupId() != null) {
                priceListIds.add(contract.getPriceListSetupId());
            }
        }
        return new LookupMaps(
                indexBy(payerIds.isEmpty() ? List.of() : nphiesPayerRepository.findByIdIn(payerIds), NphiesPayer::getId),
                indexBy(priceListIds.isEmpty() ? List.of() : priceListSetupRepository.findAllById(priceListIds), PriceListSetup::getId)
        );
    }

    private Map<Long, List<PriceListSetup>> groupPriceLists(List<PriceListSetup> lists) {
        Map<Long, List<PriceListSetup>> grouped = new HashMap<>();
        for (PriceListSetup list : lists) {
            Long payerId = list.getNphiesPayerId() != null ? list.getNphiesPayerId() : list.getPayerId();
            if (payerId != null) {
                grouped.computeIfAbsent(payerId, ignored -> new ArrayList<>()).add(list);
            }
        }
        return grouped;
    }

    private Map<Long, String> classNamesByContract(List<CoverageContract> contracts) {
        List<Long> contractIds = contracts.stream()
                .map(CoverageContract::getId)
                .filter(Objects::nonNull)
                .toList();
        if (contractIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, List<String>> names = new HashMap<>();
        for (CoverageClass coverageClass : coverageClassRepository.findByCoverageContract_IdIn(contractIds)) {
            if (coverageClass.getCoverageContract() == null || coverageClass.getCoverageContract().getId() == null) {
                continue;
            }
            names.computeIfAbsent(coverageClass.getCoverageContract().getId(), ignored -> new ArrayList<>())
                    .add(coverageClass.getName());
        }
        Map<Long, String> joined = new HashMap<>();
        names.forEach((contractId, classNames) -> joined.put(
                contractId,
                String.join(", ", classNames.stream().filter(name -> name != null && !name.isBlank()).toList())
        ));
        return joined;
    }

    private List<ContractItemVM> toContractItems(
            List<CoverageContract> contracts,
            Map<Long, NphiesPayer> payersById,
            Map<Long, PriceListSetup> priceListsById
    ) {
        Map<Long, String> classNamesByContractId = classNamesByContract(contracts);
        return contracts.stream()
                .sorted(Comparator.comparing(CoverageContract::getId, Comparator.nullsLast(Long::compareTo)))
                .map(contract -> {
                    NphiesPayer insurance = payersById.get(contract.getInsurancePayerId());
                    NphiesPayer parent = payersById.get(contract.getParentPayerId());
                    PriceListSetup priceList = priceListsById.get(contract.getPriceListSetupId());
                    return new ContractItemVM(
                            contract.getId(),
                            null,
                            contract.getGuarantorType() == null ? null : contract.getGuarantorType().name(),
                            classNamesByContractId.get(contract.getId()),
                            contract.getIsActive(),
                            contract.getInsurancePayerId(),
                            insurance == null ? null : payerDisplayName(insurance),
                            parent == null ? null : payerDisplayName(parent),
                            contract.getPriceListSetupId(),
                            priceList == null ? null : priceList.getName()
                    );
                })
                .toList();
    }

    private LinkedPartyVM toParty(NphiesPayer payer) {
        return new LinkedPartyVM(payer.getId(), payer.getNphiesId(), payerDisplayName(payer), payer.getIsActive());
    }

    private LinkedPartyVM toParty(TpaDefinition tpa) {
        return new LinkedPartyVM(tpa.getId(), tpa.getTpaCode(), tpa.getName(), tpa.getIsActive());
    }

    private PriceListItemVM toPriceListItem(PriceListSetup list) {
        return new PriceListItemVM(
                list.getId(),
                list.getName(),
                list.getStatus() == null ? null : list.getStatus().name(),
                list.getType() == null ? null : list.getType().name(),
                list.getIsActive(),
                list.getEffectiveFrom(),
                list.getEffectiveTo()
        );
    }

    private String facilityName(NphiesPayer payer) {
        if (payer.getFacility() == null || !Hibernate.isInitialized(payer.getFacility())) {
            return null;
        }
        return payer.getFacility().getName();
    }

    private String payerDisplayName(NphiesPayer payer) {
        if (payer.getNameEn() != null && !payer.getNameEn().isBlank()) {
            return payer.getNameEn();
        }
        return payer.getNameAr();
    }

    private List<LinkedPartyVM> sortedParties(List<LinkedPartyVM> parties) {
        return parties.stream().sorted(PARTY_ORDER).toList();
    }

    private <T> Map<Long, T> indexBy(List<T> items, Function<T, Long> idFn) {
        Map<Long, T> index = new LinkedHashMap<>();
        for (T item : items) {
            Long id = idFn.apply(item);
            if (id != null) {
                index.put(id, item);
            }
        }
        return index;
    }

    private record LookupMaps(
            Map<Long, NphiesPayer> payersById,
            Map<Long, PriceListSetup> priceListsById
    ) {}
}
