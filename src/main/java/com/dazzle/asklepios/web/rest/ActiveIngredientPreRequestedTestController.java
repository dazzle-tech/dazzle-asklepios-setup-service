package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.ActiveIngredientPreRequestedTest;
import com.dazzle.asklepios.service.ActiveIngredientPreRequestedTestService;
import com.dazzle.asklepios.web.rest.vm.activeIngredientPreRequestedTest.ActiveIngredientPreRequestedTestCreateVM;
import com.dazzle.asklepios.web.rest.vm.activeIngredientPreRequestedTest.ActiveIngredientPreRequestedTestResponseVM;
import com.dazzle.asklepios.web.rest.vm.activeIngredientPreRequestedTest.ActiveIngredientPreRequestedTestUpdateVM;
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
public class ActiveIngredientPreRequestedTestController {

    private static final Logger LOG = LoggerFactory.getLogger(ActiveIngredientPreRequestedTestController.class);

    private final ActiveIngredientPreRequestedTestService preRequestedTestService;

    public ActiveIngredientPreRequestedTestController(ActiveIngredientPreRequestedTestService preRequestedTestService) {
        this.preRequestedTestService = preRequestedTestService;
    }

    /**
     * POST /active-ingredient-pre-requested-tests : Create a pre-requested test.
     */
    @PostMapping("/active-ingredient-pre-requested-tests")
    public ResponseEntity<ActiveIngredientPreRequestedTestResponseVM> create( @Valid @RequestBody ActiveIngredientPreRequestedTestCreateVM vm) {
        LOG.debug("REST create ActiveIngredientPreRequestedTest payload={}", vm);
        ActiveIngredientPreRequestedTest saved = preRequestedTestService.create(vm);
        return ResponseEntity
                .created(URI.create("/api/setup/active-ingredient-pre-requested-tests/" + saved.getId()))
                .body(ActiveIngredientPreRequestedTestResponseVM.ofEntity(saved));
    }

    /**
     * PUT /active-ingredient-pre-requested-tests/{id} : Update a pre-requested test.
     * Path id must equal payload id.
     */
    @PutMapping("/active-ingredient-pre-requested-tests/{id}")
    public ResponseEntity<ActiveIngredientPreRequestedTestResponseVM> update(@PathVariable Long id, @Valid @RequestBody ActiveIngredientPreRequestedTestUpdateVM vm) {
        LOG.debug("REST update ActiveIngredientPreRequestedTest id={} payload={}", id, vm);
        if (vm.id() == null || !id.equals(vm.id())) {
            return ResponseEntity.badRequest().build();
        }
        ActiveIngredientPreRequestedTest updated = preRequestedTestService.update(vm);
        return ResponseEntity.ok(ActiveIngredientPreRequestedTestResponseVM.ofEntity(updated));
    }

    /**
     * GET /active-ingredient-pre-requested-tests/by-active-ingredient/{activeIngredientId} :
     * List pre-requested tests for an active ingredient.
     */
    @GetMapping("/active-ingredient-pre-requested-tests/by-active-ingredient/{activeIngredientId:\\d+}")
    public ResponseEntity<List<ActiveIngredientPreRequestedTestResponseVM>> getByActiveIngredientId(@PathVariable Long activeIngredientId) {
        LOG.debug("REST get pre-requested tests by activeIngredientId={}", activeIngredientId);
        List<ActiveIngredientPreRequestedTest> list = preRequestedTestService.getByActiveIngredientId(activeIngredientId);
        return ResponseEntity.ok(list.stream()
                .map(ActiveIngredientPreRequestedTestResponseVM::ofEntity)
                .toList());
    }

    /**
     * DELETE /active-ingredient-pre-requested-tests/{id} : Hard delete a pre-requested test.
     */
    @DeleteMapping("/active-ingredient-pre-requested-tests/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST delete ActiveIngredientPreRequestedTest id={}", id);
        preRequestedTestService.hardDelete(id);
        return ResponseEntity.noContent().build();
    }
}
