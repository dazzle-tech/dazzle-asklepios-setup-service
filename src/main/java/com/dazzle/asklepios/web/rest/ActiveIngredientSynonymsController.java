package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.ActiveIngredientSynonyms;
import com.dazzle.asklepios.service.ActiveIngredientSynonymsService;
import com.dazzle.asklepios.web.rest.vm.activeIngredientSynonyms.ActiveIngredientSynonymsCreateVM;
import com.dazzle.asklepios.web.rest.vm.activeIngredientSynonyms.ActiveIngredientSynonymsResponseVM;
import com.dazzle.asklepios.web.rest.vm.activeIngredientSynonyms.ActiveIngredientSynonymsUpdateVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class ActiveIngredientSynonymsController {

    private static final Logger LOG = LoggerFactory.getLogger(ActiveIngredientSynonymsController.class);

    private final ActiveIngredientSynonymsService synonymsService;

    public ActiveIngredientSynonymsController(ActiveIngredientSynonymsService synonymsService) {
        this.synonymsService = synonymsService;
    }

    /**
     * POST /active-ingredient-synonyms : Create a synonym.
     */
    @PostMapping("/active-ingredient-synonyms")
    public ResponseEntity<ActiveIngredientSynonymsResponseVM> create(@Valid @RequestBody ActiveIngredientSynonymsCreateVM vm) {
        LOG.debug("REST create ActiveIngredientSynonyms payload={}", vm);
        ActiveIngredientSynonyms saved = synonymsService.create(vm);
        return ResponseEntity
                .created(URI.create("/api/setup/active-ingredient-synonyms/" + saved.getId()))
                .body(ActiveIngredientSynonymsResponseVM.ofEntity(saved));
    }

    /**
     * PUT /active-ingredient-synonyms/{id} : Update a synonym.
     * Path id must equal payload id.
     */
    @PutMapping("/active-ingredient-synonyms/{id}")
    public ResponseEntity<ActiveIngredientSynonymsResponseVM> update(@PathVariable Long id, @Valid @RequestBody ActiveIngredientSynonymsUpdateVM vm) {
        LOG.debug("REST update ActiveIngredientSynonyms id={} payload={}", id, vm);
        if (vm.id() == null || !id.equals(vm.id())) {
            return ResponseEntity.badRequest().build();
        }
        ActiveIngredientSynonyms updated = synonymsService.update(vm);
        return ResponseEntity.ok(ActiveIngredientSynonymsResponseVM.ofEntity(updated));
    }

    /**
     * GET /active-ingredient-synonyms/by-active-ingredient/{activeIngredientId} :
     * List synonyms for an active ingredient.
     */
    @GetMapping("/active-ingredient-synonyms/by-active-ingredient/{activeIngredientId:\\d+}")
    public ResponseEntity<List<ActiveIngredientSynonymsResponseVM>> getByActiveIngredientId(@PathVariable Long activeIngredientId) {
        LOG.debug("REST get synonyms by activeIngredientId={}", activeIngredientId);
        List<ActiveIngredientSynonyms> list = synonymsService.getByActiveIngredientId(activeIngredientId);
        return ResponseEntity.ok(list.stream().map(ActiveIngredientSynonymsResponseVM::ofEntity).toList());
    }

    /**
     * DELETE /active-ingredient-synonyms/{id} : Hard delete a synonym.
     */
    @DeleteMapping("/active-ingredient-synonyms/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST delete ActiveIngredientSynonyms id={}", id);
        synonymsService.hardDelete(id);
        return ResponseEntity.noContent().build();
    }
}
