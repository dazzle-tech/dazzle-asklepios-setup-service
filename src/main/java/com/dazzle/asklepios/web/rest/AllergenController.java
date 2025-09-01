package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.Allergens;
import com.dazzle.asklepios.repository.AllergensRepository;
import com.dazzle.asklepios.service.AllergensService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/setup/api/allergen")
public class AllergenController {

    private static final Logger LOG = LoggerFactory.getLogger(AllergenController.class);

    private static final List<String> ALLOWED_ORDERED_PROPERTIES = Collections.unmodifiableList(
            Arrays.asList("id", "code", "name", "type")
    );

    private final AllergensService allergenService;
    private final AllergensRepository allergenRepository;

    public AllergenController(AllergensService allergenService, AllergensRepository allergenRepository) {
        this.allergenService = allergenService;
        this.allergenRepository = allergenRepository;
    }

    /**
     * {@code POST /api/allergen} : Create a new Allergen.
     *
     * Creates a new allergen entity from the provided request body.
     *
     * @param allergen the allergen data to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the created allergen,
     * or with status {@code 409 (Conflict)} if an allergen with the same code or name already exists.
     */
    @PostMapping
    public ResponseEntity<Allergens> create(@RequestBody Allergens allergen) {
        LOG.debug("REST request to save Allergen : {}", allergen);
        return ResponseEntity.ok(allergenService.create(allergen));
    }


    /**
     * {@code PUT /api/allergen/{id}} : Update an existing Allergen.
     *
     * @param id the id of the allergen to update.
     * @param allergen the allergen data to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated allergen,
     * or with status {@code 404 (Not Found)} if the allergen does not exist.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Allergens> updateAllergen(@PathVariable("id") Long id, @Valid @RequestBody Allergens allergen) {
        LOG.debug("REST request to update Allergen : {}, {}", id, allergen);
        Optional<Allergens> updated = allergenService.update(id, allergen);
        return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * {@code GET /api/allergen} : Get all Allergens (cached/paged).
     *
     * Returns the full list of allergens. Supports sorting by allowed properties only.
     *
     * @param pageable the paging and sorting information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of all allergens.
     */
    @GetMapping
    public ResponseEntity<List<Allergens>> getAllAllergens(Pageable pageable) {
        LOG.debug("REST request to get all Allergens (paged)");

        if (!onlyContainsAllowedProperties(pageable)) {
            return ResponseEntity.badRequest().build();
        }

        List<Allergens> allergens = allergenService.findAll();
        return ResponseEntity.ok(allergens);
    }

    /**
     * {@code GET /api/allergen/{id}} : Get the allergen by id.
     *
     * @param id the id of the allergen to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the allergen,
     * or with status {@code 404 (Not Found)} if it does not exist.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Allergens> getAllergen(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Allergen : {}", id);
        return allergenService.findOne(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * {@code DELETE /api/allergen/{id}} : Delete the allergen by id.
     *
     * @param id the id of the allergen to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
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
