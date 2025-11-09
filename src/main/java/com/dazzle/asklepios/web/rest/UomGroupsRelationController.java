package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.UomGroup;
import com.dazzle.asklepios.domain.UomGroupsRelation;
import com.dazzle.asklepios.service.UomGroupsRelationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/uom-groups/{groupId}/relations")
public class UomGroupRelationsController {

    private final UomGroupsRelationService service;

    public UomGroupsRelationsController(UomGroupsRelationService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UomGroupsRelation create(@PathVariable Long groupId,
                                    @RequestBody UomGroupsRelation body) {
        // Enforce path groupId on the domain body
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

    @GetMapping
    public List<UomGroupsRelation> list(@PathVariable Long groupId) {
        return service.listByGroup(groupId);
    }

    @GetMapping("/{id}")
    public UomGroupsRelation get(@PathVariable Long groupId, @PathVariable Long id) {
        UomGroupsRelation r = service.get(id);
        if (r.getGroup() == null || !groupId.equals(r.getGroup().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Relation not in this group");
        }
        return r;
    }

    @PutMapping("/{id}")
    public UomGroupsRelation update(@PathVariable Long groupId,
                                    @PathVariable Long id,
                                    @RequestBody UomGroupsRelation body) {
        // If caller supplies a different group in body, enforce/override with path
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
        // Safety: ensure updated relation still belongs to this group
        if (updated.getGroup() == null || !groupId.equals(updated.getGroup().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Updated relation not in this group");
        }
        return updated;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long groupId, @PathVariable Long id) {
        UomGroupsRelation r = service.get(id);
        if (r.getGroup() == null || !groupId.equals(r.getGroup().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Relation not in this group");
        }
        service.delete(id);
    }
}

