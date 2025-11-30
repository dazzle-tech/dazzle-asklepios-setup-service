package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Resource;
import com.dazzle.asklepios.domain.enumeration.ResourceType;
import com.dazzle.asklepios.repository.ResourceRepository;
import com.dazzle.asklepios.web.rest.Resource.ResourceCreateVM;
import com.dazzle.asklepios.web.rest.Resource.ResourceUpdateVM;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class ResourceService {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceService.class);
    private final ResourceRepository resourceRepository;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public Resource create(ResourceCreateVM vm) {
        LOG.debug("Request to create Resource: {}", vm);

        Resource resource = Resource.builder()
                .resourceType(vm.resourceType().name())
                .resourceKey(vm.resourceKey())
                .isAllowParallel(vm.isAllowParallel() != null ? vm.isAllowParallel() : true)
                .isActive(vm.isActive() != null ? vm.isActive() : true)
                .build();

        return resourceRepository.save(resource);
    }

    @Transactional
    public Optional<Resource> update(Long id, ResourceUpdateVM vm) {
        LOG.debug("Request to update Resource id={} with {}", id, vm);

        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Resource not found with id " + id,
                        "resource",
                        "notfound"
                ));

        if (vm.resourceType() != null) {
            resource.setResourceType(vm.resourceType().name());
        }
        if (vm.resourceKey() != null) {
            resource.setResourceKey(vm.resourceKey());
        }
        if (vm.isAllowParallel() != null) {
            resource.setIsAllowParallel(vm.isAllowParallel());
        }
        if (vm.isActive() != null) {
            resource.setIsActive(vm.isActive());
        }

        Resource updated = resourceRepository.save(resource);
        LOG.debug("Updated Resource successfully: {}", updated);

        return Optional.of(updated);
    }

    @Transactional(readOnly = true)
    public Page<Resource> findAll(Pageable pageable) {
        return resourceRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Resource> findByResourceType(ResourceType resourceType, Pageable pageable) {
        return resourceRepository.findByResourceType(resourceType.name(), pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Resource> findOne(Long id) {
        return resourceRepository.findById(id);
    }

    public Optional<Resource> toggleIsActive(Long id) {
        return resourceRepository.findById(id)
                .map(r -> {
                    r.setIsActive(!Boolean.TRUE.equals(r.getIsActive()));
                    return resourceRepository.save(r);
                });
    }
}
