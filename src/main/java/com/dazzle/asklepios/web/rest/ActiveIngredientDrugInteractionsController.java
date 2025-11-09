package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.ActiveIngredientDrugInteractions;
import com.dazzle.asklepios.service.ActiveIngredientDrugInteractionsService;
import com.dazzle.asklepios.web.rest.vm.activeIngredientDrugInteractions.ActiveIngredientDrugInteractionsCreateVM;
import com.dazzle.asklepios.web.rest.vm.activeIngredientDrugInteractions.ActiveIngredientDrugInteractionsResponseVM;
import com.dazzle.asklepios.web.rest.vm.activeIngredientDrugInteractions.ActiveIngredientDrugInteractionsUpdateVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class ActiveIngredientDrugInteractionsController {

    private static final Logger LOG = LoggerFactory.getLogger(ActiveIngredientDrugInteractionsController.class);

    private final ActiveIngredientDrugInteractionsService interactionsService;

    public ActiveIngredientDrugInteractionsController(ActiveIngredientDrugInteractionsService interactionsService) {
        this.interactionsService = interactionsService;
    }

    /**
     * POST /active-ingredient-drug-interactions : Create a drug interaction.
     */
    @PostMapping("/active-ingredient-drug-interactions")
    public ResponseEntity<ActiveIngredientDrugInteractionsResponseVM> create(@Valid @RequestBody ActiveIngredientDrugInteractionsCreateVM vm) {
        LOG.debug("REST create ActiveIngredientDrugInteractions payload={}", vm);
        ActiveIngredientDrugInteractions saved = interactionsService.create(vm);
        return ResponseEntity
                .created(URI.create("/api/setup/active-ingredient-drug-interactions/" + saved.getId()))
                .body(ActiveIngredientDrugInteractionsResponseVM.ofEntity(saved));
    }

    /**
     * PUT /active-ingredient-drug-interactions/{id} : Update a drug interaction.
     * Path id must equal payload id.
     */
    @PutMapping("/active-ingredient-drug-interactions/{id}")
    public ResponseEntity<ActiveIngredientDrugInteractionsResponseVM> update(@PathVariable Long id, @Valid @RequestBody ActiveIngredientDrugInteractionsUpdateVM vm) {
        LOG.debug("REST update ActiveIngredientDrugInteractions id={} payload={}", id, vm);
        if (vm.id() == null || !id.equals(vm.id())) {
            return ResponseEntity.badRequest().build();
        }
        ActiveIngredientDrugInteractions updated = interactionsService.update(vm);
        return ResponseEntity.ok(ActiveIngredientDrugInteractionsResponseVM.ofEntity(updated));
    }

    /**
     * GET /active-ingredient-drug-interactions/by-active-ingredient/{activeIngredientId} :
     * List drug interactions for an active ingredient.
     */
    @GetMapping("/active-ingredient-drug-interactions/by-active-ingredient/{activeIngredientId:\\d+}")
    public ResponseEntity<List<ActiveIngredientDrugInteractionsResponseVM>> getByActiveIngredientId(@PathVariable Long activeIngredientId) {
        LOG.debug("REST get drug interactions by activeIngredientId={}", activeIngredientId);
        List<ActiveIngredientDrugInteractions> list = interactionsService.getByActiveIngredientId(activeIngredientId);
        return ResponseEntity.ok(list.stream()
                .map(ActiveIngredientDrugInteractionsResponseVM::ofEntity)
                .toList());
    }

    /**
     * DELETE /active-ingredient-drug-interactions/{id} : Hard delete a drug interaction.
     */
    @DeleteMapping("/active-ingredient-drug-interactions/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST delete ActiveIngredientDrugInteractions id={}", id);
        interactionsService.hardDelete(id);
        return ResponseEntity.noContent().build();
    }
}
