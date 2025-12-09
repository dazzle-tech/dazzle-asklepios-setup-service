package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.PriceListAttribute;
import com.dazzle.asklepios.repository.PriceListAttributeRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.pricelist.PriceListAttributeSaveVM;
import com.dazzle.asklepios.web.rest.vm.pricelist.PriceListAttributeUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PriceListAttributeService {

    private static final Logger LOG = LoggerFactory.getLogger(PriceListAttributeService.class);

    private final PriceListAttributeRepository repo;

    public PriceListAttributeService(PriceListAttributeRepository repo) {
        this.repo = repo;
    }

    public PriceListAttribute create(PriceListAttributeSaveVM vm) {
        LOG.debug("Enter: create() with argument[s] = {}", vm);
        LOG.debug("Create PriceListAttribute payload={}", vm);

        PriceListAttribute a = PriceListAttribute.builder()
                .priceListId(vm.priceListId())
                .attributeType(vm.attributeType())
                .attribute(vm.attribute())
                .price(vm.price())
                .isActive(vm.isActive() != null ? vm.isActive() : true)
                .build();

        PriceListAttribute saved = repo.save(a);
        LOG.debug("Exit: create() with result = {}", saved.getId());
        return saved;
    }

    public PriceListAttribute update(PriceListAttributeUpdateVM vm) {
        LOG.debug("Enter: update() with argument[s] = {}", vm);

        PriceListAttribute existing = repo.findById(vm.id())
                .orElseThrow(() -> new BadRequestAlertException(
                        "notFound", "priceListAttribute", "Attribute not found."
                ));

        existing.setPriceListId(vm.priceListId());
        existing.setAttributeType(vm.attributeType());
        existing.setAttribute(vm.attribute());
        existing.setPrice(vm.price());
        existing.setIsActive(vm.isActive() != null ? vm.isActive() : existing.getIsActive());

        PriceListAttribute saved = repo.save(existing);
        LOG.debug("Exit: update() with result = {}", saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public java.util.Optional<PriceListAttribute> findOne(Long id) {
        LOG.debug("Enter: findOne() with argument[s] = {}", id);
        var res = repo.findById(id);
        LOG.debug("Exit: findOne() with result present = {}", res.isPresent());
        return res;
    }

    public void delete(Long id) {
        LOG.debug("Enter: delete() with argument[s] = {}", id);
        repo.deleteById(id);
        LOG.debug("Exit: delete()");
    }

    public java.util.Optional<PriceListAttribute> toggleActive(Long id) {
        LOG.debug("Enter: toggleActive() with argument[s] = {}", id);
        var res = repo.findById(id)
                .map(a -> {
                    a.setIsActive(!Boolean.TRUE.equals(a.getIsActive()));
                    return repo.save(a);
                });
        LOG.debug("Exit: toggleActive() with result present = {}", res.isPresent());
        return res;
    }

    @Transactional(readOnly = true)
    public Page<PriceListAttribute> getAll(Pageable pageable) {
        LOG.debug("Enter: getAll() with argument[s] = {}", pageable);
        var page = repo.findAll(pageable);
        LOG.debug("Exit: getAll() with result size = {}", page.getNumberOfElements());
        return page;
    }

    @Transactional(readOnly = true)
    public Page<PriceListAttribute> getAllActive(Pageable pageable) {
        LOG.debug("Enter: getAllActive() with argument[s] = {}", pageable);
        var page = repo.findByIsActiveTrue(pageable);
        LOG.debug("Exit: getAllActive() with result size = {}", page.getNumberOfElements());
        return page;
    }

    @Transactional(readOnly = true)
    public Page<PriceListAttribute> getByPriceList(Long priceListId, Pageable pageable) {
        LOG.debug("Enter: getByPriceList() priceListId={}, pageable={}", priceListId, pageable);
        var page = repo.findByPriceListId(priceListId, pageable);
        LOG.debug("Exit: getByPriceList() with result size = {}", page.getNumberOfElements());
        return page;
    }

    @Transactional(readOnly = true)
    public Page<PriceListAttribute> getActiveByPriceList(Long priceListId, Pageable pageable) {
        LOG.debug("Enter: getActiveByPriceList() priceListId={}, pageable={}", priceListId, pageable);
        var page = repo.findByPriceListIdAndIsActiveTrue(priceListId, pageable);
        LOG.debug("Exit: getActiveByPriceList() with result size = {}", page.getNumberOfElements());
        return page;
    }
}
