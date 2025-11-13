package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.ActiveIngredientSpecialPopulations;
import com.dazzle.asklepios.service.ActiveIngredientSpecialPopulationsService;
import com.dazzle.asklepios.web.rest.vm.activeIngredientSpecialPopulations.ActiveIngredientSpecialPopulationsCreateVM;
import com.dazzle.asklepios.web.rest.vm.activeIngredientSpecialPopulations.ActiveIngredientSpecialPopulationsResponseVM;
import com.dazzle.asklepios.web.rest.vm.activeIngredientSpecialPopulations.ActiveIngredientSpecialPopulationsUpdateVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class ActiveIngredientSpecialPopulationsController {

    private static final Logger LOG = LoggerFactory.getLogger(ActiveIngredientSpecialPopulationsController.class);

    private final ActiveIngredientSpecialPopulationsService specialPopulationsService;

    public ActiveIngredientSpecialPopulationsController(ActiveIngredientSpecialPopulationsService specialPopulationsService) {
        this.specialPopulationsService = specialPopulationsService;
    }

    /**
     * POST /active-ingredient-special-populations : Create a special population item.
     */
    @PostMapping("/active-ingredient-special-populations")
    public ResponseEntity<ActiveIngredientSpecialPopulationsResponseVM> create(@Valid @RequestBody ActiveIngredientSpecialPopulationsCreateVM vm) {
        LOG.debug("REST create ActiveIngredientSpecialPopulations payload={}", vm);
        ActiveIngredientSpecialPopulations saved = specialPopulationsService.create(vm);
        return ResponseEntity
                .created(URI.create("/api/setup/active-ingredient-special-populations/" + saved.getId()))
                .body(ActiveIngredientSpecialPopulationsResponseVM.ofEntity(saved));
    }

    /**
     * PUT /active-ingredient-special-populations/{id} : Update a special population item.
     * Path id must equal payload id.
     */
    @PutMapping("/active-ingredient-special-populations/{id}")
    public ResponseEntity<ActiveIngredientSpecialPopulationsResponseVM> update(@PathVariable Long id, @Valid @RequestBody ActiveIngredientSpecialPopulationsUpdateVM vm) {
        LOG.debug("REST update ActiveIngredientSpecialPopulations id={} payload={}", id, vm);
        if (vm.id() == null || !id.equals(vm.id())) {
            return ResponseEntity.badRequest().build();
        }
        ActiveIngredientSpecialPopulations updated = specialPopulationsService.update(vm);
        return ResponseEntity.ok(ActiveIngredientSpecialPopulationsResponseVM.ofEntity(updated));
    }

    /**
     * GET /active-ingredient-special-populations/by-active-ingredient/{activeIngredientId} :
     * List special population items for an active ingredient.
     */
    @GetMapping("/active-ingredient-special-populations/by-active-ingredient/{activeIngredientId:\\d+}")
    public ResponseEntity<List<ActiveIngredientSpecialPopulationsResponseVM>> getByActiveIngredientId(@PathVariable Long activeIngredientId) {
        LOG.debug("REST get special populations by activeIngredientId={}", activeIngredientId);
        List<ActiveIngredientSpecialPopulations> list = specialPopulationsService.getByActiveIngredientId(activeIngredientId);
        return ResponseEntity.ok(list.stream()
                .map(ActiveIngredientSpecialPopulationsResponseVM::ofEntity)
                .toList());
    }

    /**
     * DELETE /active-ingredient-special-populations/{id} : Hard delete a special population item.
     */
    @DeleteMapping("/active-ingredient-special-populations/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST delete ActiveIngredientSpecialPopulations id={}", id);
        specialPopulationsService.hardDelete(id);
        return ResponseEntity.noContent().build();
    }
}
