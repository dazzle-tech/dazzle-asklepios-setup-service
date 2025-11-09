package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.UomGroup;
import com.dazzle.asklepios.domain.UomGroupUnit;
import com.dazzle.asklepios.service.UomGroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// ... imports omitted
@RestController
@RequestMapping("/api/setup/uom-groups")
public class UomGroupController {

    private final UomGroupService service;


    public UomGroupController(UomGroupService service) { this.service = service; }

    // --- Groups ---
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UomGroup createGroup(@RequestBody @Valid UomGroup body) {
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

    // --- Units (keep if you want nested Units here) ---
    @PostMapping("/{groupId}/units")
    @ResponseStatus(HttpStatus.CREATED)
    public UomGroupUnit addUnit(@PathVariable Long groupId,
                                @RequestBody @Valid UomGroupUnit body) {
        return service.addUnit(groupId, body.getUom(), body.getUomOrder());
    }

    @GetMapping("/{groupId}/units")
    public List<UomGroupUnit> listUnits(@PathVariable Long groupId) {
        return service.listUnits(groupId);
    }


}
