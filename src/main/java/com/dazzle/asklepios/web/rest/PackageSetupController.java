package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.enumeration.PriceListItemType;
import com.dazzle.asklepios.service.PackageItemLookupService;
import com.dazzle.asklepios.service.PackageSetupService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.packageSetup.PackageItemLookupVM;
import com.dazzle.asklepios.web.rest.vm.packageSetup.PackageSetupResponseVM;
import com.dazzle.asklepios.web.rest.vm.packageSetup.PackageSetupSaveVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class PackageSetupController {

    private static final Logger LOG = LoggerFactory.getLogger(PackageSetupController.class);

    private final PackageSetupService packageSetupService;
    private final PackageItemLookupService packageItemLookupService;

    public PackageSetupController(
            PackageSetupService packageSetupService,
            PackageItemLookupService packageItemLookupService
    ) {
        this.packageSetupService = packageSetupService;
        this.packageItemLookupService = packageItemLookupService;
    }

    @GetMapping("/packages")
    public ResponseEntity<List<PackageSetupResponseVM>> getPackages(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal price,
            @RequestParam(required = false) Boolean isActive,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list packages code={} name={} price={} isActive={}", code, name, price, isActive);
        Page<PackageSetupResponseVM> page = packageSetupService.findAll(code, name, price, isActive, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/packages/{id}")
    public ResponseEntity<PackageSetupResponseVM> getPackage(@PathVariable Long id) {
        return ResponseEntity.ok(packageSetupService.findOne(id));
    }

    @PostMapping("/packages")
    public ResponseEntity<PackageSetupResponseVM> createPackage(@Valid @RequestBody PackageSetupSaveVM vm) {
        PackageSetupResponseVM created = packageSetupService.create(vm);
        return ResponseEntity.created(URI.create("/api/setup/packages/" + created.id())).body(created);
    }

    @PutMapping("/packages/{id}")
    public ResponseEntity<PackageSetupResponseVM> updatePackage(
            @PathVariable Long id,
            @Valid @RequestBody PackageSetupSaveVM vm
    ) {
        return ResponseEntity.ok(packageSetupService.update(id, vm));
    }

    @PatchMapping("/packages/{id}/toggle-active")
    public ResponseEntity<PackageSetupResponseVM> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(packageSetupService.toggleActive(id));
    }

    @DeleteMapping("/packages/{id}")
    public ResponseEntity<Void> deletePackage(@PathVariable Long id) {
        packageSetupService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** Active items of one type that can be added to a package, searched by name or code. */
    @GetMapping("/packages/item-lookup")
    public ResponseEntity<List<PackageItemLookupVM>> lookupItems(
            @RequestParam PriceListItemType itemType,
            @RequestParam(required = false) String search,
            @ParameterObject Pageable pageable
    ) {
        Page<PackageItemLookupVM> page = packageItemLookupService.search(itemType, search, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
}
