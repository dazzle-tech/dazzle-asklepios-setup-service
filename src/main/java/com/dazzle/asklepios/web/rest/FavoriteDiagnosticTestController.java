package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.FavoriteDiagnosticTest;
import com.dazzle.asklepios.service.FavoriteDiagnosticTestService;
import com.dazzle.asklepios.service.dto.FavoriteDiagnosticsTests.FavoriteDiagnosticTestCreateDTO;
import com.dazzle.asklepios.web.rest.vm.favoriteDiagnosticTest.FavoriteDiagnosticTestResponseVM;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/setup")
public class FavoriteDiagnosticTestController {

    private static final Logger LOG =
            LoggerFactory.getLogger(FavoriteDiagnosticTestController.class);

    private final FavoriteDiagnosticTestService favoriteDiagnosticTestService;

    public FavoriteDiagnosticTestController(
            FavoriteDiagnosticTestService favoriteDiagnosticTestService
    ) {
        this.favoriteDiagnosticTestService = favoriteDiagnosticTestService;
    }

    /* ========================= ADD ========================= */

    @PostMapping("/favorite-diagnostic-test")
    public ResponseEntity<FavoriteDiagnosticTestResponseVM> addFavorite(
            @Valid @RequestBody FavoriteDiagnosticTestCreateDTO dto
    ) {
        LOG.debug("REST create FavoriteDiagnosticTest payload={}", dto);

        FavoriteDiagnosticTest created = favoriteDiagnosticTestService.add(dto);
        FavoriteDiagnosticTestResponseVM body =
                FavoriteDiagnosticTestResponseVM.ofEntity(created);

        return ResponseEntity
                .created(
                        URI.create("/api/setup/favorite-diagnostic-test/" + created.getId())
                )
                .body(body);
    }

    /* ========================= DELETE ========================= */

    @DeleteMapping("/favorite-diagnostic-test")
    public ResponseEntity<Void> deleteFavorite(
            @RequestParam Long userId,
            @RequestParam Long testId
    ) {
        LOG.debug(
                "REST delete FavoriteDiagnosticTest userId={} testId={}",
                userId, testId
        );

        favoriteDiagnosticTestService.delete(userId, testId);
        return ResponseEntity.noContent().build();
    }

    /* ========================= LIST BY USER ========================= */

    @GetMapping("/favorite-diagnostic-test")
    public ResponseEntity<List<FavoriteDiagnosticTestResponseVM>> listByUser(
            @RequestParam Long userId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list FavoriteDiagnosticTest by userId={} pageable={}", userId, pageable);

        Page<FavoriteDiagnosticTest> page =
                favoriteDiagnosticTestService.findFavoriteDiagnosticsTestsByUserId(userId, pageable);

        HttpHeaders headers =
                com.dazzle.asklepios.web.rest.Helper.PaginationUtil
                        .generatePaginationHttpHeaders(
                                ServletUriComponentsBuilder.fromCurrentRequest(),
                                page
                        );

        List<FavoriteDiagnosticTestResponseVM> body =
                page.getContent()
                        .stream()
                        .map(FavoriteDiagnosticTestResponseVM::ofEntity)
                        .toList();

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }
}
