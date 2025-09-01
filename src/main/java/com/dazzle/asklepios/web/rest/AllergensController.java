package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.Allergens;
import com.dazzle.asklepios.repository.AllergensRepository;
import com.dazzle.asklepios.service.AllergensService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing Allergen entities.
 */
@RestController
@RequestMapping("/setup/api/allergens")
public class AllergensController {

    private static final Logger LOG = LoggerFactory.getLogger(AllergensController.class);

    private static final List<String> ALLOWED_ORDERED_PROPERTIES = List.of("id", "code", "name", "type");

    private final AllergensService allergenService;
    private final AllergensRepository allergenRepository;

    public AllergensController(AllergensService allergenService, AllergensRepository allergenRepository) {
        this.allergenService = allergenService;
        this.allergenRepository = allergenRepository;
    }

    /**
     * {@code POST /api/allergen} : Create a new Allergen.
     */
    @PostMapping
    public ResponseEntity<Allergens> createAllergen(@Valid @RequestBody Allergens allergen) {
        LOG.debug("REST request to save Allergen : {}", allergen);
        if ((allergen.getCode() != null && allergenRepository.existsByCodeIgnoreCase(allergen.getCode()))
                || (allergen.getName() != null && allergenRepository.existsByNameIgnoreCase(allergen.getName()))) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        Allergens result = allergenService.create(allergen);
        return ResponseEntity
                .created(URI.create("/setup/api/allergens/" + result.getId()))
                .body(result);
    }

    /**
     * {@code PUT /api/allergen/{id}} : Update an existing Allergen.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Allergens> updateAllergen(@PathVariable("id") Long id, @Valid @RequestBody Allergens allergen) {
        LOG.debug("REST request to update Allergen : {}, {}", id, allergen);
        Optional<Allergens> updated = allergenService.update(id, allergen);
        return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * {@code GET /api/allergen} : Get all Allergens (cached).
     */
    @GetMapping
    public ResponseEntity<List<Allergens>> getAllAllergens() {
        LOG.debug("REST request to get all Allergens (cached)");
        List<Allergens> allergens = allergenService.findAll();
        return ResponseEntity.ok(allergens);
    }


    /**
     * {@code GET /api/allergen/{id}} : Get an Allergen by id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Allergens> getAllergen(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Allergen : {}", id);
        return allergenService.findOne(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * {@code DELETE /api/allergen/{id}} : Delete an Allergen by id.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAllergen(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Allergen : {}", id);
        allergenService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Checks that all sorting properties are allowed
    private boolean onlyContainsAllowedProperties(Pageable pageable) {
        return pageable.getSort().stream()
                .map(Sort.Order::getProperty)
                .allMatch(ALLOWED_ORDERED_PROPERTIES::contains);
    }
}
