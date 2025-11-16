package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Language;
import com.dazzle.asklepios.domain.UomGroup;
import com.dazzle.asklepios.domain.UomGroupUnit;
import com.dazzle.asklepios.domain.UomGroupsRelation;
import com.dazzle.asklepios.domain.enumeration.UOM;
import com.dazzle.asklepios.repository.UomGroupRelationRepository;
import com.dazzle.asklepios.repository.UomGroupRepository;
import com.dazzle.asklepios.repository.UomGroupUnitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


@Service
@Transactional
public class UomGroupService {

    private final UomGroupRepository groupRepo;
    private final UomGroupUnitRepository unitRepo;
    private final UomGroupRelationRepository relationRepo;

    private static final Logger LOG = LoggerFactory.getLogger(UomGroupService.class);

    public UomGroupService(UomGroupRepository groupRepo,
                           UomGroupUnitRepository unitRepo,
                           UomGroupRelationRepository relationRepo) {
        this.groupRepo = groupRepo;
        this.unitRepo = unitRepo;
        this.relationRepo = relationRepo;
    }

    // Group CRUD
    public UomGroup createGroup(UomGroup g) { return groupRepo.save(g); }
    public UomGroup getGroup(Long id) { return groupRepo.findById(id).orElseThrow(); }
    public Page<UomGroup> listGroups(Pageable pageable) { return groupRepo.findAll(pageable); }
    public void deleteGroup(Long id) { groupRepo.deleteById(id); }

    public Optional<UomGroup> updateGroup(Long id, UomGroup vm) {
        LOG.debug("Request to update UOM id={} with data: {}", id, vm);

        return groupRepo.findById(id).map(existing -> {
            // Do NOT change langKey here; treat it as immutable identity
            existing.setName(vm.getName());
            existing.setDescription(vm.getDescription());
            existing.setUnits(vm.getUnits());
            existing.setRelations(vm.getRelations());
            UomGroup updated = groupRepo.save(existing);
            LOG.debug("UOMGroup id={} updated successfully", id);
            return updated;
        });
    }

    public Page<UomGroup> searchByName(String name, Pageable pageable) {
        return groupRepo.findByNameContainingIgnoreCase(name, pageable);
    }

    // Unit
    public UomGroupUnit addUnit(Long groupId, UOM uom, BigDecimal order) {
        UomGroup group = getGroup(groupId);
        UomGroupUnit unit = new UomGroupUnit();
        unit.setGroup(group);
        unit.setUom(uom);
        unit.setUomOrder(order);
        return unitRepo.save(unit);
    }
    public List<UomGroupUnit> listUnits(Long groupId) {
        return unitRepo.findByGroupId(groupId);
    }

    // Relation
    public UomGroupsRelation addRelation(Long groupId, Long fromUnitId, Long toUnitId, BigDecimal relation) {
        UomGroup group = getGroup(groupId);
        UomGroupUnit from = unitRepo.findById(fromUnitId).orElseThrow();
        UomGroupUnit to = unitRepo.findById(toUnitId).orElseThrow();

        if (!from.getGroup().getId().equals(groupId) || !to.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("Both units must belong to the same group.");
        }

        UomGroupsRelation rel = new UomGroupsRelation();
        rel.setGroup(group);
        rel.setFromUnit(from);
        rel.setToUnit(to);
        rel.setRelation(relation);
        return relationRepo.save(rel);
    }
    public List<UomGroupsRelation> listRelations(Long groupId) {
        return relationRepo.findByGroup_Id(groupId);
    }
}
