package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.BrandMedicationSubstitute;
import com.dazzle.asklepios.service.BrandMedicationSubstituteService;
import com.dazzle.asklepios.web.rest.vm.brandMedication.BrandMedicationResponseVM;
import com.dazzle.asklepios.web.rest.vm.brandMedicationSubstitute.BrandMedicationSubstituteCreateVM;
import com.dazzle.asklepios.web.rest.vm.brandMedicationSubstitute.BrandMedicationSubstituteResponseVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class BrandMedicationSubstituteController {

    private static final Logger LOG = LoggerFactory.getLogger(BrandMedicationSubstituteController.class);

    private final BrandMedicationSubstituteService service;

    public BrandMedicationSubstituteController(BrandMedicationSubstituteService service) {
        this.service = service;
    }

    /**
     * {@code POST /brand-medication-substitute} : Create a new BrandMedicationSubstitute.
     *
     * @param vm creation payload.
     * @return 201 with body and Location header.
     */
    @PostMapping("/brand-medication-substitute")
    public ResponseEntity<BrandMedicationSubstituteResponseVM> create(@Valid @RequestBody BrandMedicationSubstituteCreateVM vm) {
        LOG.debug("REST create BrandMedicationSubstitute payload={}", vm);
        BrandMedicationSubstitute created = service.create(vm);
        BrandMedicationSubstituteResponseVM body = BrandMedicationSubstituteResponseVM.ofEntity(created);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(body);
    }

    /**
     * {@code GET /brand-medication-substitute/by-brand/{brandId}} :
     * List brand medication where the given brand participates either as brand or alternative.
     */
    @GetMapping("/brand-medication-substitute/by-brand/{brandId:\\d+}")
    public ResponseEntity<List<BrandMedicationResponseVM>> listByBrand(@PathVariable Long brandId) {
        LOG.debug("REST list BrandMedicationSubstitute by brandId={}", brandId);
        List<BrandMedicationResponseVM> list = service.findAllByBrandOrAlternative(brandId)
                .stream()
                .map(BrandMedicationResponseVM::ofEntity)
                .toList();
        return ResponseEntity.ok(list);
    }

    /**
     * {@code DELETE /brand-medication-substitute/{id}} : Delete a relation.
     */
    @DeleteMapping("/brand-medication-substitute/{id:\\d+}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST delete BrandMedicationSubstitute id={}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/brand-medication-substitute/{brandId}/{altBrandId}")

    public ResponseEntity<Void> deleteSubstituteLink(
            @PathVariable long brandId,
            @PathVariable long altBrandId) {

        int deleted = service.removeSubstituteLink(brandId, altBrandId);

        if (deleted > 0) {
            return ResponseEntity.noContent().build(); // 204
        } else {
            return ResponseEntity.notFound().build(); // 404
        }
    }
    /**
     * {@code GET brand-medication-substitute/same-active-ingredient/by-brand/{brandId}} :
     * List brand medication where the given brand participates active ingredient.
     */
    @GetMapping("/brand-medication-substitute/same-active-ingredient/by-brand/{brandId:\\d+}")
    public ResponseEntity<List<BrandMedicationResponseVM>> listOfBrandWithSameActiveIngredients(@PathVariable Long brandId) {
        LOG.debug("REST list band medication by brand id={}", brandId);
        List<BrandMedicationResponseVM> list = service.findBrandsWithSameActiveIngredients(brandId)
                .stream()
                .map(BrandMedicationResponseVM::ofEntity)
                .toList();
        return ResponseEntity.ok(list);
    }
}

