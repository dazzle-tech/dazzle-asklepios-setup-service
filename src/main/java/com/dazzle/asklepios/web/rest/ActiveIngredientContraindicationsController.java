package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.ActiveIngredientContraindications;
import com.dazzle.asklepios.service.ActiveIngredientContraindicationsService;
import com.dazzle.asklepios.web.rest.vm.activeIngredientContraindications.ActiveIngredientContraindicationsCreateVM;
import com.dazzle.asklepios.web.rest.vm.activeIngredientContraindications.ActiveIngredientContraindicationsResponseVM;
import com.dazzle.asklepios.web.rest.vm.activeIngredientContraindications.ActiveIngredientContraindicationsUpdateVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * REST controller for managing {@link ActiveIngredientContraindications}.
 */
@RestController
@RequestMapping("/api/setup")
public class ActiveIngredientContraindicationsController {

    private static final Logger LOG = LoggerFactory.getLogger(ActiveIngredientContraindicationsController.class);

    private final ActiveIngredientContraindicationsService contraindicationsService;

    public ActiveIngredientContraindicationsController(ActiveIngredientContraindicationsService contraindicationsService) {
        this.contraindicationsService = contraindicationsService;
    }

    /**
     * {@code POST /active-ingredient-contraindications} : Create a new contraindication.
     *
     * @param activeIngredientContraindicationsCreateVM the creation payload.
     * @return {@link ResponseEntity} with status 201 (Created) and the created entity in body.
     */
    @PostMapping("/active-ingredient-contraindications")
    public ResponseEntity<ActiveIngredientContraindicationsResponseVM> create(@Valid @RequestBody ActiveIngredientContraindicationsCreateVM activeIngredientContraindicationsCreateVM) {
        LOG.debug("REST create ActiveIngredientContraindications payload={}", activeIngredientContraindicationsCreateVM);
        ActiveIngredientContraindications saved = contraindicationsService.create(activeIngredientContraindicationsCreateVM);
        return ResponseEntity
                .created(URI.create("/api/setup/active-ingredient-contraindications/" + saved.getId()))
                .body(ActiveIngredientContraindicationsResponseVM.ofEntity(saved));
    }

    /**
     * {@code PUT /active-ingredient-contraindications/{id}} : Update an existing contraindication.
     * <p>
     * Enforces path id equals payload id.
     *
     * @param id                                        the contraindication id.
     * @param activeIngredientContraindicationsUpdateVM the update payload.
     * @return {@link ResponseEntity} with status 200 (OK) and updated entity in body.
     */
    @PutMapping("/active-ingredient-contraindications/{id}")
    public ResponseEntity<ActiveIngredientContraindicationsResponseVM> update(@PathVariable Long id, @Valid @RequestBody ActiveIngredientContraindicationsUpdateVM activeIngredientContraindicationsUpdateVM) {
        LOG.debug("REST update ActiveIngredientContraindications id={} payload={}", id, activeIngredientContraindicationsUpdateVM);
        if (activeIngredientContraindicationsUpdateVM.id() == null || !id.equals(activeIngredientContraindicationsUpdateVM.id())) {
            return ResponseEntity.badRequest().build();
        }
        ActiveIngredientContraindications updated = contraindicationsService.update(activeIngredientContraindicationsUpdateVM);
        return ResponseEntity.ok(ActiveIngredientContraindicationsResponseVM.ofEntity(updated));
    }

    /**
     * {@code GET /active-ingredient-contraindications/by-active-ingredient/{activeIngredientId}} :
     * List contraindications by active ingredient id.
     *
     * @param activeIngredientId the active ingredient id.
     * @return list of {@link ActiveIngredientContraindicationsResponseVM}.
     */
    @GetMapping("/active-ingredient-contraindications/by-active-ingredient/{activeIngredientId:\\d+}")
    public ResponseEntity<List<ActiveIngredientContraindicationsResponseVM>> getByActiveIngredientId(@PathVariable Long activeIngredientId) {
        LOG.debug("REST get contraindications by activeIngredientId={}", activeIngredientId);
        List<ActiveIngredientContraindications> list = contraindicationsService.getByActiveIngredientId(activeIngredientId);
        return ResponseEntity.ok(
                list.stream().map(ActiveIngredientContraindicationsResponseVM::ofEntity).toList()
        );
    }

    /**
     * {@code DELETE /active-ingredient-contraindications/{id}} : Hard delete.
     *
     * @param id the indication id.
     * @return {@link ResponseEntity} with status 204 (No Content).
     */
    @DeleteMapping("/active-ingredient-contraindications/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST delete ActiveIngredientContraindications id={}", id);
        contraindicationsService.hardDelete(id);
        return ResponseEntity.noContent().build();
    }
}
