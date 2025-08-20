package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.Screen;
import com.dazzle.asklepios.service.ScreenService;
import com.dazzle.asklepios.repository.ScreenRepository;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing Screens.
 */
@RestController
@RequestMapping("/setup/api/screens")
public class ScreenController {

    private static final Logger LOG = LoggerFactory.getLogger(ScreenController.class);

    private static final List<String> ALLOWED_ORDERED_PROPERTIES = Collections.unmodifiableList(
            Arrays.asList("id", "name", "module")
    );

    private final ScreenService screenService;
    private final ScreenRepository screenRepository;

    public ScreenController(ScreenService screenService, ScreenRepository screenRepository) {
        this.screenService = screenService;
        this.screenRepository = screenRepository;
    }

    /**
     * {@code POST /screens} : Create a new screen.
     *
     * @param screen the screen to create
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new screen,
     * or with status {@code 409 (Conflict)} if a screen with the same name already exists for the module.
     */
    @PostMapping
    public ResponseEntity<Screen> createScreen(@Valid @RequestBody Screen screen) {
        LOG.debug("REST request to save Screen : {}", screen);

        if (screen.getModule() != null && screen.getName() != null &&
                screenService.existsByModuleAndName(screen.getModule().getId(), screen.getName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Screen result = screenService.create(screen);
        return ResponseEntity
                .created(URI.create("/setup/api/screens/" + result.getId()))
                .body(result);
    }

    /**
     * {@code PUT /screens/{id}} : Update an existing screen.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Screen> updateScreen(@PathVariable("id") Long id, @Valid @RequestBody Screen screen) {
        LOG.debug("REST request to update Screen : {}, {}", id, screen);
        Optional<Screen> updated = screenService.update(id, screen);
        return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * {@code GET /screens} : Get all screens.
     */
    @GetMapping
    public ResponseEntity<List<Screen>> getAllScreens() {
        LOG.debug("REST request to get all Screens");
        List<Screen> screens = screenService.findAll();
        return ResponseEntity.ok(screens);
    }

    /**
     * {@code GET /screens/{id}} : Get screen by id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Screen> getScreen(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Screen : {}", id);
        return screenService.findOne(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * {@code DELETE /screens/{id}} : Delete screen by id.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScreen(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Screen : {}", id);
        screenService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
