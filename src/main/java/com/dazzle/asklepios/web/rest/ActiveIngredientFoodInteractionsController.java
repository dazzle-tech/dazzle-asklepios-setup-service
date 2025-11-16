package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.ActiveIngredientFoodInteractions;
import com.dazzle.asklepios.service.ActiveIngredientFoodInteractionsService;
import com.dazzle.asklepios.web.rest.vm.activeIngredientFoodInteractions.ActiveIngredientFoodInteractionsCreateVM;
import com.dazzle.asklepios.web.rest.vm.activeIngredientFoodInteractions.ActiveIngredientFoodInteractionsResponseVM;
import com.dazzle.asklepios.web.rest.vm.activeIngredientFoodInteractions.ActiveIngredientFoodInteractionsUpdateVM;
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
public class ActiveIngredientFoodInteractionsController {

    private static final Logger LOG = LoggerFactory.getLogger(ActiveIngredientFoodInteractionsController.class);

    private final ActiveIngredientFoodInteractionsService foodInteractionsService;

    public ActiveIngredientFoodInteractionsController(ActiveIngredientFoodInteractionsService foodInteractionsService) {
        this.foodInteractionsService = foodInteractionsService;
    }

    /**
     * POST /active-ingredient-food-interactions : Create a food interaction.
     */
    @PostMapping("/active-ingredient-food-interactions")
    public ResponseEntity<ActiveIngredientFoodInteractionsResponseVM> create(@Valid @RequestBody ActiveIngredientFoodInteractionsCreateVM vm) {
        LOG.debug("REST create ActiveIngredientFoodInteractions payload={}", vm);
        ActiveIngredientFoodInteractions saved = foodInteractionsService.create(vm);
        return ResponseEntity
                .created(URI.create("/api/setup/active-ingredient-food-interactions/" + saved.getId()))
                .body(ActiveIngredientFoodInteractionsResponseVM.ofEntity(saved));
    }

    /**
     * PUT /active-ingredient-food-interactions/{id} : Update a food interaction.
     * Path id must equal payload id.
     */
    @PutMapping("/active-ingredient-food-interactions/{id}")
    public ResponseEntity<ActiveIngredientFoodInteractionsResponseVM> update(@PathVariable Long id, @Valid @RequestBody ActiveIngredientFoodInteractionsUpdateVM vm) {
        LOG.debug("REST update ActiveIngredientFoodInteractions id={} payload={}", id, vm);
        if (vm.id() == null || !id.equals(vm.id())) {
            return ResponseEntity.badRequest().build();
        }
        ActiveIngredientFoodInteractions updated = foodInteractionsService.update(vm);
        return ResponseEntity.ok(ActiveIngredientFoodInteractionsResponseVM.ofEntity(updated));
    }

    /**
     * GET /active-ingredient-food-interactions/by-active-ingredient/{activeIngredientId} :
     * List food interactions for an active ingredient.
     */
    @GetMapping("/active-ingredient-food-interactions/by-active-ingredient/{activeIngredientId:\\d+}")
    public ResponseEntity<List<ActiveIngredientFoodInteractionsResponseVM>> getByActiveIngredientId(@PathVariable Long activeIngredientId) {
        LOG.debug("REST get food interactions by activeIngredientId={}", activeIngredientId);
        List<ActiveIngredientFoodInteractions> list = foodInteractionsService.getByActiveIngredientId(activeIngredientId);
        return ResponseEntity.ok(list.stream()
                .map(ActiveIngredientFoodInteractionsResponseVM::ofEntity)
                .toList());
    }

    /**
     * DELETE /active-ingredient-food-interactions/{id} : Hard delete a food interaction.
     */
    @DeleteMapping("/active-ingredient-food-interactions/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST delete ActiveIngredientFoodInteractions id={}", id);
        foodInteractionsService.hardDelete(id);
        return ResponseEntity.noContent().build();
    }
}
