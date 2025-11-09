package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.UomGroup;
import com.dazzle.asklepios.domain.UomGroupUnit;
import com.dazzle.asklepios.domain.UomGroupsRelation;
import com.dazzle.asklepios.service.UomGroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/uom-groups")
public class UomGroupController {

    private final UomGroupService service;

    public UomGroupController(UomGroupService service) {
        this.service = service;
    }

    // --- Groups ---
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UomGroup createGroup(@RequestBody @Valid UomGroup body) {
        // domain in, domain out
        return service.createGroup(body);
    }

    @GetMapping
    public List<UomGroup> listGroups() {
        return service.listGroups();
    }

    @GetMapping("/{id}")
    public UomGroup getGroup(@PathVariable Long id) {
        return service.getGroup(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGroup(@PathVariable Long id) {
        service.deleteGroup(id);
    }

    // --- Units ---
    @PostMapping("/{groupId}/units")
    @ResponseStatus(HttpStatus.CREATED)
    public UomGroupUnit addUnit(@PathVariable Long groupId,
                                @RequestBody @Valid UomGroupUnit body) {
        // use the values from the domain body, attach via path groupId
        return service.addUnit(groupId, body.getUom(), body.getUomOrder());
    }

    @GetMapping("/{groupId}/units")
    public List<UomGroupUnit> listUnits(@PathVariable Long groupId) {
        return service.listUnits(groupId);
    }

    // --- Relations ---
    @PostMapping("/{groupId}/relations")
    @ResponseStatus(HttpStatus.CREATED)
    public UomGroupsRelation addRelation(@PathVariable Long groupId,
                                         @RequestBody @Valid UomGroupsRelation body) {
        // domain body with nested refs: {"fromUnit":{"id":..}, "toUnit":{"id":..}, "relation": ...}
        Long fromUnitId = body.getFromUnit() != null ? body.getFromUnit().getId() : null;
        Long toUnitId   = body.getToUnit() != null ? body.getToUnit().getId() : null;
        return service.addRelation(groupId, fromUnitId, toUnitId, body.getRelation());
    }

    @GetMapping("/{groupId}/relations")
    public List<UomGroupsRelation> listRelations(@PathVariable Long groupId) {
        return service.listRelations(groupId);
    }
}
