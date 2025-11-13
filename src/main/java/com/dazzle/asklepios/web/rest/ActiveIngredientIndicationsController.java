package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.ActiveIngredientIndications;
import com.dazzle.asklepios.service.ActiveIngredientIndicationsService;
import com.dazzle.asklepios.web.rest.vm.activeIngredientIndications.ActiveIngredientIndicationsCreateVM;
import com.dazzle.asklepios.web.rest.vm.activeIngredientIndications.ActiveIngredientIndicationsResponseVM;
import com.dazzle.asklepios.web.rest.vm.activeIngredientIndications.ActiveIngredientIndicationsUpdateVM;
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

/**
 * REST controller for managing {@link ActiveIngredientIndications}.
 */
@RestController
@RequestMapping("/api/setup")
public class ActiveIngredientIndicationsController {

    private static final Logger LOG = LoggerFactory.getLogger(ActiveIngredientIndicationsController.class);

    private final ActiveIngredientIndicationsService activeIngredientIndicationsService;

    public ActiveIngredientIndicationsController(ActiveIngredientIndicationsService activeIngredientIndicationsService) {
        this.activeIngredientIndicationsService = activeIngredientIndicationsService;
    }

    /**
     * {@code POST /active-ingredient-indications} : Create a new ActiveIngredientIndications.
     *
     * @param activeIngredientIndicationsCreateVM the creation payload.
     * @return {@link ResponseEntity} with status 201 (Created) and the created entity in body.
     */
    @PostMapping("/active-ingredient-indications")
    public ResponseEntity<ActiveIngredientIndicationsResponseVM> create(@Valid @RequestBody ActiveIngredientIndicationsCreateVM activeIngredientIndicationsCreateVM) {
        LOG.debug("REST create ActiveIngredientIndications payload={}", activeIngredientIndicationsCreateVM);
        ActiveIngredientIndications saved = activeIngredientIndicationsService.create(activeIngredientIndicationsCreateVM);
        ActiveIngredientIndicationsResponseVM response = ActiveIngredientIndicationsResponseVM.ofEntity(saved);

        return ResponseEntity
                .created(URI.create("/api/setup/active-ingredient-indications/" + saved.getId()))
                .body(response);
    }

    /**
     * {@code PUT /active-ingredient-indications/{id}} : Update an existing ActiveIngredientIndications.
     *
     * @param id                                  the indication id.
     * @param activeIngredientIndicationsUpdateVM the update payload.
     * @return {@link ResponseEntity} with status 200 (OK) and updated entity in body.
     */
    @PutMapping("/active-ingredient-indications/{id}")
    public ResponseEntity<ActiveIngredientIndicationsResponseVM> update(@PathVariable Long id, @Valid @RequestBody ActiveIngredientIndicationsUpdateVM activeIngredientIndicationsUpdateVM) {
        LOG.debug("REST update ActiveIngredientIndications id={} payload={}", id, activeIngredientIndicationsUpdateVM);
        if (activeIngredientIndicationsUpdateVM.id() == null || !id.equals(activeIngredientIndicationsUpdateVM.id())) {
            return ResponseEntity.badRequest().build();
        }
        ActiveIngredientIndications updated = activeIngredientIndicationsService.update(activeIngredientIndicationsUpdateVM);
        return ResponseEntity.ok(ActiveIngredientIndicationsResponseVM.ofEntity(updated));
    }

    /**
     * {@code GET /active-ingredient-indications/by-active-ingredient/{activeIngredientId}} :
     * Get all indications for a specific active ingredient.
     *
     * @param activeIngredientId the active ingredient id.
     * @return list of {@link ActiveIngredientIndicationsResponseVM}.
     */
    @GetMapping("/active-ingredient-indications/by-active-ingredient/{activeIngredientId:\\d+}")
    public ResponseEntity<List<ActiveIngredientIndicationsResponseVM>> getByActiveIngredientId(@PathVariable Long activeIngredientId) {
        LOG.debug("REST get ActiveIngredientIndications by activeIngredientId={}", activeIngredientId);
        List<ActiveIngredientIndications> list = activeIngredientIndicationsService.getByActiveIngredientId(activeIngredientId);

        List<ActiveIngredientIndicationsResponseVM> response = list.stream().map(ActiveIngredientIndicationsResponseVM::ofEntity).toList();
        return ResponseEntity.ok(response);
    }

    /**
     * {@code DELETE /active-ingredient-indications/{id}} : Hard delete an indication.
     *
     * @param id the indication id.
     * @return {@link ResponseEntity} with status 204 (No Content).
     */
    @DeleteMapping("/active-ingredient-indications/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST delete ActiveIngredientIndications id={}", id);
        activeIngredientIndicationsService.hardDelete(id);
        return ResponseEntity.noContent().build();
    }
}
