package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.InventoryProducts;
import com.dazzle.asklepios.domain.enumeration.InventoryType;
import com.dazzle.asklepios.domain.enumeration.ProductTypes;
import com.dazzle.asklepios.service.InventoryProductsService;
import com.dazzle.asklepios.service.dto.InventoryProductsCreateDTO;
import com.dazzle.asklepios.service.dto.InventoryProductsUpdateDTO;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.inventoryProducts.InventoryProductsResponseVM;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class InventoryProductsController {

    private static final Logger LOG = LoggerFactory.getLogger(InventoryProductsController.class);

    private final InventoryProductsService inventoryProductsService;

    public InventoryProductsController(InventoryProductsService inventoryProductsService) {
        this.inventoryProductsService = inventoryProductsService;
    }

    /**
     * {@code POST /inventory-products} : Create a new inventory product.
     * Validates payload, checks uniqueness (name+type, code+type), persists the entity,
     * and returns a response model.
     *
     * @param inventoryProductsCreateDTO the creation payload.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and body of the
     *         created inventory product, with a {@code Location} header pointing to the new resource;
     *         or {@code 400 (Bad Request)} if the payload is invalid or uniqueness is violated.
     */
    @PostMapping("/inventory-products")
    public ResponseEntity<InventoryProductsResponseVM> create(@Valid @RequestBody InventoryProductsCreateDTO inventoryProductsCreateDTO) {
        LOG.debug("REST create InventoryProducts payload={}", inventoryProductsCreateDTO);
        InventoryProducts saved = inventoryProductsService.create(inventoryProductsCreateDTO);
        InventoryProductsResponseVM body = InventoryProductsResponseVM.ofEntity(saved);

        return ResponseEntity
                .created(URI.create("/api/inventory/inventory-products/" + saved.getId()))
                .body(body);
    }

    /**
     * {@code PUT /inventory-products/{id}} : Update an existing InventoryProducts.
     * Updates mutable fields of the product identified by {@code id}.
     *
     * @param id the identifier of the inventory product to update.
     * @param inventoryProductsUpdateDTO the update payload.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the updated inventory product in the body,
     *         or {@code 400 (Bad Request)} if id mismatch or uniqueness violated,
     *         or {@code 404 (Not Found)} if no inventory product exists with the given id.
     */
    @PutMapping("/inventory-products/{id}")
    public ResponseEntity<InventoryProductsResponseVM> update(@PathVariable Long id, @Valid @RequestBody InventoryProductsUpdateDTO inventoryProductsUpdateDTO) {
        LOG.debug("REST update InventoryProducts id={} payload={}", id, inventoryProductsUpdateDTO);
        if (inventoryProductsUpdateDTO.id() == null || !id.equals(inventoryProductsUpdateDTO.id())) {
            throw new BadRequestAlertException(
                    "An inventory product id should not be null.",
                    "InventoryProducts",
                    "error.validation"
            );
        }
        InventoryProducts updated = inventoryProductsService.update(inventoryProductsUpdateDTO);
        return ResponseEntity.ok(InventoryProductsResponseVM.ofEntity(updated));
    }

    /**
     * {@code GET /inventory-products} : Get a paginated list of all inventory products.
     * Supports standard Spring pagination parameters:
     * {@code page}, {@code size}, and {@code sort}.
     *
     * @param pageable the pagination and sorting information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}, a list of inventory
     *         products view models in the body, and pagination headers.
     */
    @GetMapping("/inventory-products")
    public ResponseEntity<List<InventoryProductsResponseVM>> getAll(@ParameterObject Pageable pageable) {
        LOG.debug("REST list InventoryProducts page={}", pageable);
        Page<InventoryProducts> page = inventoryProductsService.getAll(pageable);
        HttpHeaders headers =
                PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);

        List<InventoryProductsResponseVM> body = page.getContent()
                .stream()
                .map(InventoryProductsResponseVM::ofEntity)
                .toList();

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }
    /**
     * {@code GET /inventory-products/{id}} : Get a single product by id.
     *
     * @param id the identifier of the product to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the product view model,
     *         or {@code 404 (Not Found)} if the product does not exist.
     */
    @GetMapping("/inventory-products/{id}")
    public ResponseEntity<InventoryProductsResponseVM> getInventoryProduct(@PathVariable Long id) {
        LOG.debug("REST get inventory product id={}", id);
        return inventoryProductsService.findOne(id)
                .map(InventoryProductsResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * {@code GET /inventory-products/by-name/{name}} : Get inventory products by name (contains, ignore case).
     *
     * @param name the name filter.
     * @param pageable pagination and sorting information.
     * @return {@link ResponseEntity} with status {@code 200 (OK)} and list of view models plus pagination headers.
     */
    @GetMapping("/inventory-products/by-name/{name}")
    public ResponseEntity<List<InventoryProductsResponseVM>> getByName(
            @PathVariable String name,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list InventoryProducts by name='{}' page={}", name, pageable);
        Page<InventoryProducts> page = inventoryProductsService.getByName(name, pageable);
        HttpHeaders headers =
                PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);

        List<InventoryProductsResponseVM> body = page.getContent()
                .stream()
                .map(InventoryProductsResponseVM::ofEntity)
                .toList();

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    /**
     * {@code GET /inventory-products/by-type/{type}} : Get inventory products by product type.
     *
     * @param type the product type enum.
     * @param pageable pagination and sorting information.
     * @return {@link ResponseEntity} with status {@code 200 (OK)} and list of view models plus pagination headers.
     */
    @GetMapping("/inventory-products/by-type/{type}")
    public ResponseEntity<List<InventoryProductsResponseVM>> getByProductType(
            @PathVariable ProductTypes type,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list InventoryProducts by type='{}' page={}", type, pageable);
        Page<InventoryProducts> page = inventoryProductsService.getByProductType(type, pageable);
        HttpHeaders headers =
                PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);

        List<InventoryProductsResponseVM> body = page.getContent()
                .stream()
                .map(InventoryProductsResponseVM::ofEntity)
                .toList();

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    /**
     * {@code GET /inventory-products/by-base-uom/{baseUom}} : Get inventory products by base UOM.
     *
     * @param baseUom the base unit of measure.
     * @param pageable pagination and sorting information.
     * @return {@link ResponseEntity} with status {@code 200 (OK)} and list of view models plus pagination headers.
     */
    @GetMapping("/inventory-products/by-base-uom/{baseUom}")
    public ResponseEntity<List<InventoryProductsResponseVM>> getByBaseUom(
            @PathVariable String baseUom,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list InventoryProducts by baseUom='{}' page={}", baseUom, pageable);
        Page<InventoryProducts> page = inventoryProductsService.getByBaseUom(baseUom, pageable);
        HttpHeaders headers =
                PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);

        List<InventoryProductsResponseVM> body = page.getContent()
                .stream()
                .map(InventoryProductsResponseVM::ofEntity)
                .toList();

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    /**
     * {@code GET /inventory-products/by-inventory-type/{inventoryType}} :
     * Get inventory products by inventory type.
     *
     * @param inventoryType the inventory type enum.
     * @param pageable pagination and sorting information.
     * @return {@link ResponseEntity} with status {@code 200 (OK)} and list of view models plus pagination headers.
     */
    @GetMapping("/inventory-products/by-inventory-type/{inventoryType}")
    public ResponseEntity<List<InventoryProductsResponseVM>> getByInventoryType(
            @PathVariable InventoryType inventoryType,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list InventoryProducts by inventoryType='{}' page={}", inventoryType, pageable);
        Page<InventoryProducts> page = inventoryProductsService.getByInventoryType(inventoryType, pageable);
        HttpHeaders headers =
                PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);

        List<InventoryProductsResponseVM> body = page.getContent()
                .stream()
                .map(InventoryProductsResponseVM::ofEntity)
                .toList();

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    /**
     * {@code PATCH /inventory-products/{id}/toggle-active} :
     * Toggle the {@code isActive} status of an inventory product.
     *
     * @param id the identifier of the inventory product to toggle.
     * @return {@link ResponseEntity} with status {@code 200 (OK)} and updated view model,
     *         or {@code 404 (Not Found)} if the product does not exist.
     */
    @PatchMapping("/inventory-products/{id}/toggle-active")
    public ResponseEntity<InventoryProductsResponseVM> toggleActive(@PathVariable Long id) {
        LOG.debug("REST toggle InventoryProducts isActive id={}", id);
        InventoryProducts product = inventoryProductsService.toggleActive(id);
        return ResponseEntity.ok(InventoryProductsResponseVM.ofEntity(product));
    }
}