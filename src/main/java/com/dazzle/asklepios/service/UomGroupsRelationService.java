package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.UomGroup;
import com.dazzle.asklepios.domain.UomGroupUnit;
import com.dazzle.asklepios.domain.UomGroupsRelation;
import com.dazzle.asklepios.repository.UomGroupRelationRepository;
import com.dazzle.asklepios.repository.UomGroupRepository;
import com.dazzle.asklepios.repository.UomGroupUnitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class UomGroupsRelationService {

    private final UomGroupRepository groupRepo;
    private final UomGroupUnitRepository unitRepo;
    private final UomGroupRelationRepository relRepo;

    public UomGroupsRelationService(UomGroupRepository groupRepo,
                                    UomGroupUnitRepository unitRepo,
                                    UomGroupRelationRepository relRepo) {
        this.groupRepo = groupRepo;
        this.unitRepo = unitRepo;
        this.relRepo = relRepo;
    }

    /* =========================
       Domain-in / Domain-out
       ========================= */

    public UomGroupsRelation create(UomGroupsRelation body) {
        Long groupId    = (body.getGroup()    != null) ? body.getGroup().getId()    : null;
        Long fromUnitId = (body.getFromUnit() != null) ? body.getFromUnit().getId() : null;
        Long toUnitId   = (body.getToUnit()   != null) ? body.getToUnit().getId()   : null;

        if (groupId == null || fromUnitId == null || toUnitId == null) {
            throw new IllegalArgumentException("group.id, fromUnit.id and toUnit.id are required");
        }

        UomGroup group = groupRepo.findById(groupId).orElseThrow();
        UomGroupUnit from = unitRepo.findById(fromUnitId).orElseThrow();
        UomGroupUnit to = unitRepo.findById(toUnitId).orElseThrow();

        if (!groupId.equals(from.getGroup().getId()) || !groupId.equals(to.getGroup().getId())) {
            throw new IllegalArgumentException("Both units must belong to the specified group.");
        }

        // Persist a managed relation (ignore any id on the incoming body for create)
        UomGroupsRelation rel = new UomGroupsRelation();
        rel.setGroup(group);
        rel.setFromUnit(from);
        rel.setToUnit(to);
        rel.setRelation(body.getRelation());

        return relRepo.save(rel);
    }

    public UomGroupsRelation update(Long id, UomGroupsRelation body) {
        UomGroupsRelation rel = relRepo.findById(id).orElseThrow();

        if (body.getRelation() != null) {
            rel.setRelation(body.getRelation());
        }

        // Optionally allow changing group/from/to via body
        Long newGroupId    = (body.getGroup()    != null) ? body.getGroup().getId()    : null;
        Long newFromUnitId = (body.getFromUnit() != null) ? body.getFromUnit().getId() : null;
        Long newToUnitId   = (body.getToUnit()   != null) ? body.getToUnit().getId()   : null;

        if (newGroupId != null) {
            UomGroup g = groupRepo.findById(newGroupId).orElseThrow();
            rel.setGroup(g);
        }
        if (newFromUnitId != null) {
            UomGroupUnit from = unitRepo.findById(newFromUnitId).orElseThrow();
            rel.setFromUnit(from);
        }
        if (newToUnitId != null) {
            UomGroupUnit to = unitRepo.findById(newToUnitId).orElseThrow();
            rel.setToUnit(to);
        }

        // Final consistency check
        Long gid = rel.getGroup().getId();
        if (!gid.equals(rel.getFromUnit().getGroup().getId()) || !gid.equals(rel.getToUnit().getGroup().getId())) {
            throw new IllegalArgumentException("fromUnit and toUnit must belong to relation.group");
        }

        return rel; // managed entity; changes will be flushed
    }

    public UomGroupsRelation create(Long groupId, Long fromUnitId, Long toUnitId, BigDecimal relation) {
        UomGroupsRelation tmp = new UomGroupsRelation();
        tmp.setRelation(relation);
        UomGroup g = new UomGroup(); g.setId(groupId); tmp.setGroup(g);
        UomGroupUnit fu = new UomGroupUnit(); fu.setId(fromUnitId); tmp.setFromUnit(fu);
        UomGroupUnit tu = new UomGroupUnit(); tu.setId(toUnitId); tmp.setToUnit(tu);
        return create(tmp);
    }

    public UomGroupsRelation update(Long id, BigDecimal relation) {
        UomGroupsRelation tmp = new UomGroupsRelation();
        tmp.setRelation(relation);
        return update(id, tmp);
    }

    @Transactional(readOnly = true)
    public UomGroupsRelation get(Long id) { return relRepo.findById(id).orElseThrow(); }

    @Transactional(readOnly = true)
    public List<UomGroupsRelation> listByGroup(Long groupId) { return relRepo.findByGroup_Id(groupId); }

    public void delete(Long id) { relRepo.deleteById(id); }
}
