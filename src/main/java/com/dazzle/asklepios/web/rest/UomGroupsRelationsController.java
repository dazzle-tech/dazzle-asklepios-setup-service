package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.UomGroup;
import com.dazzle.asklepios.domain.UomGroupUnit;
import com.dazzle.asklepios.domain.UomGroupsRelation;
import com.dazzle.asklepios.service.UomGroupsRelationService;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.uom.UomGroupsRelationResponseVM;
import com.dazzle.asklepios.web.rest.vm.uom.UomGroupsRelationVM;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.hibernate.id.IdentifierGenerator.ENTITY_NAME;

@RestController
@RequestMapping("/api/setup/uom-groups/relations")
public class UomGroupsRelationsController {

    private final UomGroupsRelationService service;

    public UomGroupsRelationsController(UomGroupsRelationService service) {
        this.service = service;
    }

    @PostMapping("/{groupId}")
    @ResponseStatus(HttpStatus.CREATED)
    public UomGroupsRelationResponseVM create(@PathVariable Long groupId,
                                              @Valid @RequestBody UomGroupsRelationVM body) {
        var saved = service.create(groupId, body.fromUnitId(), body.toUnitId(), body.relation());
        return UomGroupsRelationResponseVM.of(saved);
    }

    @GetMapping("/{groupId}")
    public List<UomGroupsRelation> list(@PathVariable Long groupId) {
        return service.listByGroup(groupId);
    }


    @PutMapping("/{groupId}/{id}")
    public UomGroupsRelationResponseVM update(@PathVariable Long groupId,
                                              @PathVariable Long id,
                                              @Valid @RequestBody UomGroupsRelationVM body) {
        var tmp = new UomGroupsRelation();
        var g = new UomGroup();
        g.setId(groupId);
        tmp.setGroup(g);
        tmp.setRelation(body.relation());
        if (body.fromUnitId() != null) {
            var fu = new UomGroupUnit();
            fu.setId(body.fromUnitId());
            tmp.setFromUnit(fu);
        }
        if (body.toUnitId() != null) {
            var tu = new UomGroupUnit();
            tu.setId(body.toUnitId());
            tmp.setToUnit(tu);
        }
        var updated = service.update(id, tmp);
        if (!groupId.equals(updated.getGroup().getId())) {
            throw new BadRequestAlertException("Updated relation not in this group", ENTITY_NAME, "Relation not in this group");
        }
        return UomGroupsRelationResponseVM.of(updated);
    }

    // Get one: GET /api/setup/uom-groups/relations/{groupId}/{id}
    @GetMapping("/{groupId}/{id}")
    public UomGroupsRelation get(@PathVariable Long groupId, @PathVariable Long id) {
        UomGroupsRelation r = service.get(id);
        if (r.getGroup() == null || !groupId.equals(r.getGroup().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Relation not in this group");
        }
        return r;
    }


    // Delete: DELETE /api/setup/uom-groups/relations/{groupId}/{id}
    @DeleteMapping("/{groupId}/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long groupId, @PathVariable Long id) {
        UomGroupsRelation r = service.get(id);
        if (r.getGroup() == null || !groupId.equals(r.getGroup().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Relation not in this group");
        }
        service.delete(id);
    }
}
