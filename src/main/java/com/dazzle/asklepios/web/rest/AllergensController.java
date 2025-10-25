package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.Allergens;
import com.dazzle.asklepios.domain.enumeration.AllergenType;
import com.dazzle.asklepios.service.AllergensService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.allergens.AllergensCreateVM;
import com.dazzle.asklepios.web.rest.vm.allergens.AllergensResponseVM;
import com.dazzle.asklepios.web.rest.vm.allergens.AllergensUpdateVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class AllergensController {

    private static final Logger LOG = LoggerFactory.getLogger(AllergensController.class);

    private final AllergensService allergenService;

    public AllergensController(AllergensService allergenService) {
        this.allergenService = allergenService;
    }

    // ====================== CREATE ======================
    @PostMapping("/allergen")
    public ResponseEntity<AllergensResponseVM> createAllergen(
            @Valid @RequestBody AllergensCreateVM vm
    ) {
        LOG.debug("REST create Allergen payload={}", vm);

        Allergens toCreate = Allergens.builder()
                .code(vm.code())
                .name(vm.name())
                .type(vm.type())
                .description(vm.description())
                .isActive(Boolean.TRUE.equals(vm.isActive()))
                .build();

        Allergens created = allergenService.create(toCreate);
        AllergensResponseVM body = AllergensResponseVM.ofEntity(created);

        return ResponseEntity
                .created(URI.create("/api/setup/allergen/" + created.getId()))
                .body(body);
    }

    // ====================== UPDATE ======================
    @PutMapping("/allergen/{id}")
    public ResponseEntity<AllergensResponseVM> updateAllergen(
            @PathVariable Long id,
            @Valid @RequestBody AllergensUpdateVM vm
    ) {
        LOG.debug("REST update Allergen id={} payload={}", id, vm);

        Allergens patch = new Allergens();
        patch.setCode(vm.code());
        patch.setName(vm.name());
        patch.setType(vm.type());
        patch.setDescription(vm.description());
        patch.setIsActive(vm.isActive());

        return allergenService.update(id, patch)
                .map(AllergensResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ====================== READ (Paged) ======================
    @GetMapping("/allergen")
    public ResponseEntity<List<AllergensResponseVM>> getAllAllergensPaged(
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list Allergens pageable={}", pageable);
        final Page<Allergens> page = allergenService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        List<AllergensResponseVM> body = page.getContent()
                .stream()
                .map(AllergensResponseVM::ofEntity)
                .toList();

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    // ====================== FILTERS ======================
    @GetMapping("/allergen/by-type/{type}")
    public ResponseEntity<List<AllergensResponseVM>> getByType(
            @PathVariable AllergenType type,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list Allergens by type={} pageable={}", type, pageable);
        Page<Allergens> page = allergenService.findByType(type, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        List<AllergensResponseVM> body = page.getContent()
                .stream()
                .map(AllergensResponseVM::ofEntity)
                .toList();

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    @GetMapping("/allergen/by-code/{code}")
    public ResponseEntity<List<AllergensResponseVM>> getByCode(
            @PathVariable String code,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list Allergens by code='{}' pageable={}", code, pageable);
        Page<Allergens> page = allergenService.findByCodeContainingIgnoreCase(code, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        List<AllergensResponseVM> body = page.getContent()
                .stream()
                .map(AllergensResponseVM::ofEntity)
                .toList();

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    @GetMapping("/allergen/by-name/{name}")
    public ResponseEntity<List<AllergensResponseVM>> getByName(
            @PathVariable String name,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list Allergens by name='{}' pageable={}", name, pageable);
        Page<Allergens> page = allergenService.findByNameContainingIgnoreCase(name, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        List<AllergensResponseVM> body = page.getContent()
                .stream()
                .map(AllergensResponseVM::ofEntity)
                .toList();

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    // ====================== READ ONE ======================
    @GetMapping("/allergen/{id}")
    public ResponseEntity<AllergensResponseVM> getAllergen(@PathVariable Long id) {
        LOG.debug("REST get Allergen id={}", id);
        return allergenService.findOne(id)
                .map(AllergensResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ====================== DELETE ======================
    @DeleteMapping("/allergen/{id}")
    public ResponseEntity<Void> deleteAllergen(@PathVariable Long id) {
        LOG.debug("REST delete Allergen id={}", id);
        allergenService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ======================  TOGGLE ACTIVE ======================
    @PatchMapping("/allergen/{id}/toggle-active")
    public ResponseEntity<AllergensResponseVM> toggleActive(@PathVariable Long id) {
        LOG.debug("REST toggle Allergen isActive id={}", id);
        return allergenService.toggleIsActive(id)
                .map(AllergensResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
