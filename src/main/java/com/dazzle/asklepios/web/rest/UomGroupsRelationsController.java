package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.UomGroup;
import com.dazzle.asklepios.domain.UomGroupsRelation;
import com.dazzle.asklepios.service.UomGroupsRelationService;
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

@RestController
@RequestMapping("/api/setup/uom-groups/relations")
public class UomGroupsRelationsController {

    private final UomGroupsRelationService service;

    public UomGroupsRelationsController(UomGroupsRelationService service) {
        this.service = service;
    }

    // Create: POST /api/setup/uom-groups/relations/{groupId}
    @PostMapping("/{groupId}")
    @ResponseStatus(HttpStatus.CREATED)
    public UomGroupsRelation create(@PathVariable Long groupId,
                                    @RequestBody UomGroupsRelation body) {
        if (body.getGroup() == null) {
            UomGroup g = new UomGroup();
            g.setId(groupId);
            body.setGroup(g);
        } else if (body.getGroup().getId() == null) {
            body.getGroup().setId(groupId);
        } else if (!groupId.equals(body.getGroup().getId())) {
            throw new IllegalArgumentException("Path groupId and body.group.id must match");
        }
        return service.create(body);
    }

    // List: GET /api/setup/uom-groups/relations/{groupId}
    @GetMapping("/{groupId}")
    public List<UomGroupsRelation> list(@PathVariable Long groupId) {
        return service.listByGroup(groupId);
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

    // Update: PUT /api/setup/uom-groups/relations/{groupId}/{id}
    @PutMapping("/{groupId}/{id}")
    public UomGroupsRelation update(@PathVariable Long groupId,
                                    @PathVariable Long id,
                                    @RequestBody UomGroupsRelation body) {
        if (body.getGroup() == null) {
            UomGroup g = new UomGroup();
            g.setId(groupId);
            body.setGroup(g);
        } else if (body.getGroup().getId() == null) {
            body.getGroup().setId(groupId);
        } else if (!groupId.equals(body.getGroup().getId())) {
            throw new IllegalArgumentException("Path groupId and body.group.id must match");
        }

        UomGroupsRelation updated = service.update(id, body);
        if (updated.getGroup() == null || !groupId.equals(updated.getGroup().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Updated relation not in this group");
        }
        return updated;
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
