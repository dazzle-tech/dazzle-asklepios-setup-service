package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.UomGroup;
import com.dazzle.asklepios.domain.UomGroupUnit;
import com.dazzle.asklepios.domain.UomGroupsRelation;
import com.dazzle.asklepios.domain.enumeration.UOM;
import com.dazzle.asklepios.repository.UomGroupRelationRepository;
import com.dazzle.asklepios.repository.UomGroupRepository;
import com.dazzle.asklepios.repository.UomGroupUnitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class UomGroupService {

    private final UomGroupRepository groupRepo;
    private final UomGroupUnitRepository unitRepo;
    private final UomGroupRelationRepository relationRepo;

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
    public List<UomGroup> listGroups() { return groupRepo.findAll(); }
    public void deleteGroup(Long id) { groupRepo.deleteById(id); }

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
        return unitRepo.findByGroup_Id(groupId);
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
