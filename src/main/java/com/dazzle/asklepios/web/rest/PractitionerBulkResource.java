package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.Practitioner;
import com.dazzle.asklepios.service.PractitionerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/setup/practitioner")
public class PractitionerBulkResource {

    private final PractitionerService practitionerService;

    public PractitionerBulkResource(PractitionerService practitionerService) {
        this.practitionerService = practitionerService;
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<Practitioner>> getBulk(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(practitionerService.findByIds(ids));
    }
}
