package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.Pharmaceutical;
import com.dazzle.asklepios.service.PharmaceuticalService;
import com.dazzle.asklepios.web.rest.vm.pharmaceutical.PharmaceuticalCreateVM;
import com.dazzle.asklepios.web.rest.vm.pharmaceutical.PharmaceuticalResponseVM;
import com.dazzle.asklepios.web.rest.vm.pharmaceutical.PharmaceuticalUpdateVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * Pharmaceuticals are referenced by prescriptions, so they are deactivated ({@code isActive=false}) instead of deleted.
 */
@RestController
@RequestMapping("/api/setup")
public class PharmaceuticalController {

    private static final Logger LOG = LoggerFactory.getLogger(PharmaceuticalController.class);

    private final PharmaceuticalService pharmaceuticalService;

    public PharmaceuticalController(PharmaceuticalService pharmaceuticalService) {
        this.pharmaceuticalService = pharmaceuticalService;
    }

    @PostMapping("/pharmaceuticals")
    public ResponseEntity<PharmaceuticalResponseVM> create(@Valid @RequestBody PharmaceuticalCreateVM vm) {
        LOG.debug("REST create Pharmaceutical payload={}", vm);
        Pharmaceutical saved = pharmaceuticalService.create(vm);
        return ResponseEntity
                .created(URI.create("/api/setup/pharmaceuticals/" + saved.getId()))
                .body(PharmaceuticalResponseVM.ofEntity(saved));
    }

    @PutMapping("/pharmaceuticals/{id}")
    public ResponseEntity<PharmaceuticalResponseVM> update(@PathVariable Long id, @Valid @RequestBody PharmaceuticalUpdateVM vm) {
        LOG.debug("REST update Pharmaceutical id={} payload={}", id, vm);
        if (vm.id() == null || !id.equals(vm.id())) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(PharmaceuticalResponseVM.ofEntity(pharmaceuticalService.update(vm)));
    }

    @GetMapping("/pharmaceuticals/{id:\\d+}")
    public ResponseEntity<PharmaceuticalResponseVM> getById(@PathVariable Long id) {
        LOG.debug("REST get Pharmaceutical id={}", id);
        return ResponseEntity.ok(PharmaceuticalResponseVM.ofEntity(pharmaceuticalService.getById(id)));
    }

    @GetMapping("/pharmaceuticals/by-active-ingredient/{activeIngredientId:\\d+}")
    public ResponseEntity<List<PharmaceuticalResponseVM>> getByActiveIngredientId(@PathVariable Long activeIngredientId) {
        LOG.debug("REST get Pharmaceuticals by activeIngredientId={}", activeIngredientId);
        return ResponseEntity.ok(pharmaceuticalService.getByActiveIngredientId(activeIngredientId)
                .stream().map(PharmaceuticalResponseVM::ofEntity).toList());
    }

    /**
     * GET /pharmaceuticals/by-ids?ids=1,2 : pharmaceuticals by ids (including inactive).
     */
    @GetMapping("/pharmaceuticals/by-ids")
    public ResponseEntity<List<PharmaceuticalResponseVM>> getByIds(@RequestParam List<Long> ids) {
        LOG.debug("REST get Pharmaceuticals by ids={}", ids);
        return ResponseEntity.ok(pharmaceuticalService.getByIds(ids)
                .stream().map(PharmaceuticalResponseVM::ofEntity).toList());
    }

    /**
     * GET /pharmaceuticals?search=: active pharmaceuticals for the prescription picker.
     */
    @GetMapping("/pharmaceuticals")
    public ResponseEntity<List<PharmaceuticalResponseVM>> searchActive(@RequestParam(required = false) String search) {
        LOG.debug("REST search active Pharmaceuticals search='{}'", search);
        return ResponseEntity.ok(pharmaceuticalService.searchActive(search)
                .stream().map(PharmaceuticalResponseVM::ofEntity).toList());
    }
}
