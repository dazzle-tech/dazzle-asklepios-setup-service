package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.ActiveIngredientAdverseEffects;
import com.dazzle.asklepios.service.ActiveIngredientAdverseEffectsService;
import com.dazzle.asklepios.web.rest.vm.activeIngredientAdverseEffects.ActiveIngredientAdverseEffectsCreateVM;
import com.dazzle.asklepios.web.rest.vm.activeIngredientAdverseEffects.ActiveIngredientAdverseEffectsResponseVM;
import com.dazzle.asklepios.web.rest.vm.activeIngredientAdverseEffects.ActiveIngredientAdverseEffectsUpdateVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * REST controller for managing {@link ActiveIngredientAdverseEffects}.
 */
@RestController
@RequestMapping("/api/setup")
public class ActiveIngredientAdverseEffectsController {

    private static final Logger LOG = LoggerFactory.getLogger(ActiveIngredientAdverseEffectsController.class);

    private final ActiveIngredientAdverseEffectsService adverseEffectsService;

    public ActiveIngredientAdverseEffectsController(ActiveIngredientAdverseEffectsService adverseEffectsService) {
        this.adverseEffectsService = adverseEffectsService;
    }

    /**
     * {@code POST /active-ingredient-adverse-effects} : Create a new adverse effect record.
     *
     * @param activeIngredientAdverseEffectsCreateVM the creation payload.
     * @return {@link ResponseEntity} with status 201 (Created) and the created entity in body.
     */
    @PostMapping("/active-ingredient-adverse-effects")
    public ResponseEntity<ActiveIngredientAdverseEffectsResponseVM> create(@Valid @RequestBody ActiveIngredientAdverseEffectsCreateVM activeIngredientAdverseEffectsCreateVM) {
        LOG.debug("REST create ActiveIngredientAdverseEffects payload={}", activeIngredientAdverseEffectsCreateVM);
        ActiveIngredientAdverseEffects saved = adverseEffectsService.create(activeIngredientAdverseEffectsCreateVM);
        return ResponseEntity
                .created(URI.create("/api/setup/active-ingredient-adverse-effects/" + saved.getId()))
                .body(ActiveIngredientAdverseEffectsResponseVM.ofEntity(saved));
    }

    /**
     * {@code PUT /active-ingredient-adverse-effects/{id}} : Update an existing adverse effect record.
     * Enforces id equality between path and payload.
     *
     * @param id                                     the adverse effect id.
     * @param activeIngredientAdverseEffectsUpdateVM the update payload.
     * @return {@link ResponseEntity} with status 200 (OK) and updated entity in body.
     */
    @PutMapping("/active-ingredient-adverse-effects/{id}")
    public ResponseEntity<ActiveIngredientAdverseEffectsResponseVM> update(@PathVariable Long id, @Valid @RequestBody ActiveIngredientAdverseEffectsUpdateVM activeIngredientAdverseEffectsUpdateVM) {
        LOG.debug("REST update ActiveIngredientAdverseEffects id={} payload={}", id, activeIngredientAdverseEffectsUpdateVM);
        if (activeIngredientAdverseEffectsUpdateVM.id() == null || !id.equals(activeIngredientAdverseEffectsUpdateVM.id())) {
            return ResponseEntity.badRequest().build();
        }
        ActiveIngredientAdverseEffects updated = adverseEffectsService.update(activeIngredientAdverseEffectsUpdateVM);
        return ResponseEntity.ok(ActiveIngredientAdverseEffectsResponseVM.ofEntity(updated));
    }

    /**
     * {@code GET /active-ingredient-adverse-effects/by-active-ingredient/{activeIngredientId}} :
     * List adverse effects by active ingredient id.
     *
     * @param activeIngredientId the active ingredient id.
     * @return list of {@link ActiveIngredientAdverseEffectsResponseVM}.
     */
    @GetMapping("/active-ingredient-adverse-effects/by-active-ingredient/{activeIngredientId:\\d+}")
    public ResponseEntity<List<ActiveIngredientAdverseEffectsResponseVM>> getByActiveIngredientId(@PathVariable Long activeIngredientId) {
        LOG.debug("REST get adverse effects by activeIngredientId={}", activeIngredientId);
        List<ActiveIngredientAdverseEffects> list = adverseEffectsService.getByActiveIngredientId(activeIngredientId);
        return ResponseEntity.ok(list.stream().map(ActiveIngredientAdverseEffectsResponseVM::ofEntity).toList());
    }

    /**
     * {@code DELETE /active-ingredient-adverse-effects/{id}} : Hard delete an adverse effect record.
     *  @param id the adverse effect id.
     *  @return {@link ResponseEntity} with status 204 (No Content).
     */
    @DeleteMapping("/active-ingredient-adverse-effects/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST delete ActiveIngredientAdverseEffects id={}", id);
        adverseEffectsService.hardDelete(id);
        return ResponseEntity.noContent().build();
    }
}
