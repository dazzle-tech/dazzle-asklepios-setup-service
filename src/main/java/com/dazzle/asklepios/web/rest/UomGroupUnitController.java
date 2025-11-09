package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.UomGroupUnit;
import com.dazzle.asklepios.service.UomGroupUnitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/setup/uom-units")
public class UomGroupUnitController {

    private final UomGroupUnitService service;

    public UomGroupUnitController(UomGroupUnitService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UomGroupUnit create(@RequestBody @Valid UomGroupUnit body) {
        Long groupId = (body.getGroup() != null) ? body.getGroup().getId() : null;
        if (groupId == null) {
            throw new IllegalArgumentException("group.id is required");
        }
        return service.create(groupId, body.getUom(), body.getUomOrder());
    }

    @PutMapping("/{id}")
    public UomGroupUnit update(@PathVariable Long id, @RequestBody @Valid UomGroupUnit body) {
        return service.update(id, body.getUom(), body.getUomOrder());
    }

    @GetMapping("/{id}")
    public UomGroupUnit get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping
    public List<UomGroupUnit> listByGroup(@RequestParam Long groupId) {
        return service.listByGroup(groupId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
