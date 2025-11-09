package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.UomGroup;
import com.dazzle.asklepios.domain.UomGroupUnit;
import com.dazzle.asklepios.domain.enumeration.UOM;
import com.dazzle.asklepios.repository.UomGroupRepository;
import com.dazzle.asklepios.repository.UomGroupUnitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class UomGroupUnitService {

    private final UomGroupRepository groupRepo;
    private final UomGroupUnitRepository unitRepo;

    public UomGroupUnitService(UomGroupRepository groupRepo, UomGroupUnitRepository unitRepo) {
        this.groupRepo = groupRepo;
        this.unitRepo = unitRepo;
    }

    public UomGroupUnit create(Long groupId, UOM uom, BigDecimal order) {
        UomGroup group = groupRepo.findById(groupId).orElseThrow();
        UomGroupUnit unit = new UomGroupUnit();
        unit.setGroup(group);
        unit.setUom(uom);
        unit.setUomOrder(order);
        return unitRepo.save(unit);
    }

    public UomGroupUnit update(Long id, UOM uom, BigDecimal order) {
        UomGroupUnit unit = unitRepo.findById(id).orElseThrow();
        unit.setUom(uom);
        unit.setUomOrder(order);
        return unit;
    }

    @Transactional(readOnly = true)
    public UomGroupUnit get(Long id) { return unitRepo.findById(id).orElseThrow(); }

    @Transactional(readOnly = true)
    public List<UomGroupUnit> listByGroup(Long groupId) {
        return unitRepo.findByGroupId(groupId);
    }

    public void delete(Long id) { unitRepo.deleteById(id); }
}
