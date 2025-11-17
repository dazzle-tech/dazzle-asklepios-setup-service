package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.UomGroup;
import com.dazzle.asklepios.domain.UomGroupUnit;
import com.dazzle.asklepios.service.UomGroupService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.uom.UomGroupVM;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/setup/uom-groups")
public class UomGroupController {

    private final UomGroupService service;


    public UomGroupController(UomGroupService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UomGroup createGroup(@RequestBody @Valid UomGroup body) {
        return service.createGroup(body);
    }

    @PutMapping("/{id}")
    public Optional<UomGroup> updateGroup(@PathVariable Long id, @RequestBody @Valid UomGroupVM body) {
        return service.updateGroup(id, body);
    }
    @GetMapping
    public ResponseEntity<Page<UomGroup>> listGroups(@RequestParam(required = false) String name, @ParameterObject Pageable pageable) {

        if (name != null && !name.isBlank()) {
            return ResponseEntity.ok(service.searchByName(name.trim(), pageable));
        }
        return ResponseEntity.ok(service.listGroups(pageable));
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
