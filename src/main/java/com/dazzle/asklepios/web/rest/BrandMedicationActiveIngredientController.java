package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.BrandMedicationActiveIngredient;
import com.dazzle.asklepios.service.BrandMedicationActiveIngredientService;
import com.dazzle.asklepios.web.rest.vm.brandMedicationActiveIngredient.BrandMedicationActiveIngredientCreateVM;
import com.dazzle.asklepios.web.rest.vm.brandMedicationActiveIngredient.BrandMedicationActiveIngredientResponseVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class BrandMedicationActiveIngredientController {

    private static final Logger LOG = LoggerFactory.getLogger(BrandMedicationActiveIngredientController.class);

    private final BrandMedicationActiveIngredientService service;

    public BrandMedicationActiveIngredientController(BrandMedicationActiveIngredientService service) {
        this.service = service;
    }

    /**
     * {@code POST /brand-medication-active-ingredient} : Create a new BrandMedicationActiveIngredient.
     */
    @PostMapping("/brand-medication-active-ingredient")
    public ResponseEntity<BrandMedicationActiveIngredientResponseVM> create(@Valid @RequestBody BrandMedicationActiveIngredientCreateVM vm) {
        LOG.debug("REST create BrandMedicationActiveIngredient payload={}", vm);
        BrandMedicationActiveIngredient created = service.create(vm);
        BrandMedicationActiveIngredientResponseVM body = BrandMedicationActiveIngredientResponseVM.ofEntity(created);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(body);
    }

    /**
     * {@code GET /brand-medication-active-ingredient/by-brand/{brandId}} :
     * List active ingredients linked to a brand medication.
     */
    @GetMapping("/brand-medication-active-ingredient/by-brand/{brandId:\\d+}")
    public ResponseEntity<List<BrandMedicationActiveIngredientResponseVM>> listByBrand(@PathVariable Long brandId) {
        LOG.debug("REST list BrandMedicationActiveIngredient by brandId={}", brandId);
        List<BrandMedicationActiveIngredientResponseVM> list = service.findAllByBrandMedication(brandId)
                .stream()
                .map(BrandMedicationActiveIngredientResponseVM::ofEntity)
                .toList();
        return ResponseEntity.ok(list);
    }

    /**
     * {@code DELETE /brand-medication-active-ingredient/{id}} : Delete a brand-active-ingredient relation.
     */
    @DeleteMapping("/brand-medication-active-ingredient/{id:\\d+}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST delete BrandMedicationActiveIngredient id={}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

}
