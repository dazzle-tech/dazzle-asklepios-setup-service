package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.SkillDefinition;
import com.dazzle.asklepios.service.SkillDefinitionService;
import com.dazzle.asklepios.service.dto.skillDefinition.SkillDefinitionCreateDTO;
import com.dazzle.asklepios.service.dto.skillDefinition.SkillDefinitionUpdateDTO;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.skillDefinition.SkillDefinitionResponseVM;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
@RequiredArgsConstructor
public class SkillDefinitionController {

    private static final Logger LOG = LoggerFactory.getLogger(SkillDefinitionController.class);

    private final SkillDefinitionService skillDefinitionService;

    @PostMapping("/skill-definition")
    public ResponseEntity<SkillDefinitionResponseVM> createSkillDefinition(
            @Valid @RequestBody SkillDefinitionCreateDTO skillDefinitionCreateDTO
    ) {
        LOG.debug("REST request to create SkillDefinition : {}", skillDefinitionCreateDTO);

        SkillDefinition created = skillDefinitionService.create(skillDefinitionCreateDTO);

        return ResponseEntity
                .created(URI.create("/api/setup/skill-definition/" + created.getId()))
                .body(SkillDefinitionResponseVM.ofEntity(created));
    }

    @PutMapping("/skill-definition")
    public ResponseEntity<SkillDefinitionResponseVM> updateSkillDefinition(
            @Valid @RequestBody SkillDefinitionUpdateDTO skillDefinitionUpdateDTO
    ) {
        LOG.debug("REST request to update SkillDefinition : {}", skillDefinitionUpdateDTO);

        SkillDefinition updated = skillDefinitionService.update(skillDefinitionUpdateDTO);

        return ResponseEntity.ok(SkillDefinitionResponseVM.ofEntity(updated));
    }

    @GetMapping("/skill-definition")
    public ResponseEntity<List<SkillDefinitionResponseVM>> getAllSkillDefinition(
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get all SkillDefinition");

        Page<SkillDefinition> page = skillDefinitionService.findAll(pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        List<SkillDefinitionResponseVM> body = page.getContent().stream()
                .map(SkillDefinitionResponseVM::ofEntity)
                .toList();

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    @GetMapping("/skill-definition/by-facility")
    public ResponseEntity<List<SkillDefinitionResponseVM>> getAllSkillDefinitionByFacility(
            @RequestParam Long facilityId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get SkillDefinition by facilityId={}", facilityId);

        Page<SkillDefinition> page = skillDefinitionService.findByFacilityId(facilityId, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        List<SkillDefinitionResponseVM> body = page.getContent().stream()
                .map(SkillDefinitionResponseVM::ofEntity)
                .toList();

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    @GetMapping("/skill-definition/by-code")
    public ResponseEntity<List<SkillDefinitionResponseVM>> getAllSkillDefinitionByCode(
            @RequestParam String code,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get SkillDefinition by code='{}'", code);

        Page<SkillDefinition> page = skillDefinitionService.findByCode(code, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        List<SkillDefinitionResponseVM> body = page.getContent().stream()
                .map(SkillDefinitionResponseVM::ofEntity)
                .toList();

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    @GetMapping("/skill-definition/by-name")
    public ResponseEntity<List<SkillDefinitionResponseVM>> getAllSkillDefinitionByName(
            @RequestParam String name,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get SkillDefinition by name='{}'", name);

        Page<SkillDefinition> page = skillDefinitionService.findByName(name, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        List<SkillDefinitionResponseVM> body = page.getContent().stream()
                .map(SkillDefinitionResponseVM::ofEntity)
                .toList();

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    @GetMapping("/skill-definition/by-type")
    public ResponseEntity<List<SkillDefinitionResponseVM>> getAllSkillDefinitionByType(
            @RequestParam String type,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get SkillDefinition by type='{}'", type);

        Page<SkillDefinition> page = skillDefinitionService.findByType(type, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        List<SkillDefinitionResponseVM> body = page.getContent().stream()
                .map(SkillDefinitionResponseVM::ofEntity)
                .toList();

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    @GetMapping("/skill-definition/{id}")
    public ResponseEntity<SkillDefinitionResponseVM> getOneSkillDefinitionById(
            @PathVariable Long id
    ) {
        LOG.debug("REST request to get SkillDefinition : {}", id);

        return skillDefinitionService.findOne(id)
                .map(SkillDefinitionResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/skill-definition/{id}/toggle-active")
    public ResponseEntity<SkillDefinitionResponseVM> toggleSkillDefinitionActive(
            @PathVariable Long id
    ) {
        LOG.debug("REST request to toggle active SkillDefinition : {}", id);

        return skillDefinitionService.toggleActive(id)
                .map(SkillDefinitionResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}