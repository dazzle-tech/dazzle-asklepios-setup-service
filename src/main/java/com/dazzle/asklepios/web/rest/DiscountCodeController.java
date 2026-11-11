package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.DiscountCode;
import com.dazzle.asklepios.domain.enumeration.DiscountType;
import com.dazzle.asklepios.service.DiscountCodeAllowedUserService;
import com.dazzle.asklepios.service.DiscountCodeAuthorizationService;
import com.dazzle.asklepios.service.DiscountCodeService;
import com.dazzle.asklepios.service.dto.discountCode.DiscountCodeAllowedUsersUpdateDTO;
import com.dazzle.asklepios.service.dto.discountCode.DiscountCodeCreateDTO;
import com.dazzle.asklepios.service.dto.discountCode.DiscountCodeUpdateDTO;
import com.dazzle.asklepios.service.dto.discountCode.DiscountCodeValidateDTO;
import com.dazzle.asklepios.service.dto.discountCode.DiscountCodeVerifyUserDTO;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.discountCode.DiscountCodeAllowedUserVM;
import com.dazzle.asklepios.web.rest.vm.discountCode.DiscountCodeResponseVM;
import com.dazzle.asklepios.web.rest.vm.discountCode.DiscountCodeVerifiedUserVM;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
@RequiredArgsConstructor
public class DiscountCodeController {

    private static final Logger LOG = LoggerFactory.getLogger(DiscountCodeController.class);

    private final DiscountCodeService discountCodeService;
    private final DiscountCodeAllowedUserService discountCodeAllowedUserService;
    private final DiscountCodeAuthorizationService discountCodeAuthorizationService;

    @PostMapping("/discount-code")
    public ResponseEntity<DiscountCodeResponseVM> create(
            @Valid @RequestBody DiscountCodeCreateDTO dto
    ) {
        LOG.debug("REST request to create DiscountCode : {}", dto);
        DiscountCode created = discountCodeService.create(dto);
        return ResponseEntity
                .created(URI.create("/api/setup/discount-code/" + created.getId()))
                .body(DiscountCodeResponseVM.ofEntity(created));
    }

    @PutMapping("/discount-code")
    public ResponseEntity<DiscountCodeResponseVM> update(
            @Valid @RequestBody DiscountCodeUpdateDTO dto
    ) {
        LOG.debug("REST request to update DiscountCode : {}", dto);
        DiscountCode updated = discountCodeService.update(dto);
        return ResponseEntity.ok(DiscountCodeResponseVM.ofEntity(updated));
    }

    @GetMapping("/discount-code")
    public ResponseEntity<List<DiscountCodeResponseVM>> getAll(@ParameterObject Pageable pageable) {
        LOG.debug("REST request to get all DiscountCode");
        return paged(discountCodeService.findAll(pageable));
    }

    @GetMapping("/discount-code/by-facility")
    public ResponseEntity<List<DiscountCodeResponseVM>> getByFacility(
            @RequestParam Long facilityId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get DiscountCode by facilityId={}", facilityId);
        return paged(discountCodeService.findByFacilityId(facilityId, pageable));
    }

    @GetMapping("/discount-code/by-code")
    public ResponseEntity<List<DiscountCodeResponseVM>> getByCode(
            @RequestParam String code,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get DiscountCode by code='{}'", code);
        return paged(discountCodeService.findByCode(code, pageable));
    }

    @GetMapping("/discount-code/by-name")
    public ResponseEntity<List<DiscountCodeResponseVM>> getByName(
            @RequestParam String name,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get DiscountCode by name='{}'", name);
        return paged(discountCodeService.findByName(name, pageable));
    }

    @GetMapping("/discount-code/by-type")
    public ResponseEntity<List<DiscountCodeResponseVM>> getByType(
            @RequestParam DiscountType discountType,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get DiscountCode by discountType={}", discountType);
        return paged(discountCodeService.findByDiscountType(discountType, pageable));
    }

    @PostMapping("/discount-code/verify-user")
    public ResponseEntity<DiscountCodeVerifiedUserVM> verifyUser(
            @Valid @RequestBody DiscountCodeVerifyUserDTO dto
    ) {
        LOG.debug("REST request to verify discount-code user login={}", dto.login());
        return ResponseEntity.ok(discountCodeAuthorizationService.verifyUser(dto));
    }

    @PostMapping("/discount-code/validate")
    public ResponseEntity<DiscountCodeResponseVM> validate(
            @Valid @RequestBody DiscountCodeValidateDTO dto
    ) {
        LOG.debug(
                "REST request to validate discount code facilityId={} userId={}",
                dto.facilityId(),
                dto.userId()
        );
        return ResponseEntity.ok(
                DiscountCodeResponseVM.ofEntity(discountCodeAuthorizationService.validate(dto))
        );
    }

    @GetMapping("/discount-code/{id}")
    public ResponseEntity<DiscountCodeResponseVM> getOne(@PathVariable Long id) {
        LOG.debug("REST request to get DiscountCode : {}", id);
        return ResponseEntity.ok(DiscountCodeResponseVM.ofEntity(discountCodeService.findOne(id)));
    }

    @GetMapping("/discount-code/{id}/allowed-users")
    public ResponseEntity<List<DiscountCodeAllowedUserVM>> getAllowedUsers(@PathVariable Long id) {
        LOG.debug("REST request to get allowed users for DiscountCode : {}", id);
        return ResponseEntity.ok(discountCodeAllowedUserService.findByDiscountCodeId(id));
    }

    @PutMapping("/discount-code/{id}/allowed-users")
    public ResponseEntity<List<DiscountCodeAllowedUserVM>> replaceAllowedUsers(
            @PathVariable Long id,
            @Valid @RequestBody DiscountCodeAllowedUsersUpdateDTO dto
    ) {
        LOG.debug("REST request to replace allowed users for DiscountCode id={} userIds={}", id, dto.userIds());
        return ResponseEntity.ok(discountCodeAllowedUserService.replace(id, dto.userIds()));
    }

    @PatchMapping("/discount-code/{id}/toggle-active")
    public ResponseEntity<DiscountCodeResponseVM> toggleActive(@PathVariable Long id) {
        LOG.debug("REST request to toggle active DiscountCode : {}", id);
        return ResponseEntity.ok(DiscountCodeResponseVM.ofEntity(discountCodeService.toggleActive(id)));
    }

    private ResponseEntity<List<DiscountCodeResponseVM>> paged(Page<DiscountCode> page) {
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );
        List<DiscountCodeResponseVM> body = page.getContent()
                .stream()
                .map(DiscountCodeResponseVM::ofEntity)
                .toList();
        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }
}
