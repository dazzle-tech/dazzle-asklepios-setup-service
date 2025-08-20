package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Module;
import com.dazzle.asklepios.repository.ModuleRepository;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ModuleService {

    private static final Logger LOG = LoggerFactory.getLogger(ModuleService.class);

    private final ModuleRepository moduleRepository;

    public ModuleService(ModuleRepository moduleRepository) {
        this.moduleRepository = moduleRepository;
    }

    // ================= Create =================
    @CacheEvict(cacheNames = ModuleRepository.MODULES, key = "'all'")
    public Module create(Module module) {
        LOG.debug("Request to create Module : {}", module);
        module.setId(null); // ensure a new entity
        return moduleRepository.save(module);
    }

    // ================= Update =================
    @CacheEvict(cacheNames = ModuleRepository.MODULES, key = "'all'")
    public Optional<Module> update(Long id, Module module) {
        LOG.debug("Request to update Module id={} with : {}", id, module);
        return moduleRepository
                .findById(id)
                .map(existing -> {
                    existing.setName(module.getName());
                    existing.setDescription(module.getDescription());
                    return moduleRepository.save(existing);
                });
    }

    // ================= Read =================
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = ModuleRepository.MODULES, key = "'all'")
    public List<Module> findAll() {
        LOG.debug("Request to get all Modules");
        return moduleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Module> findOne(Long id) {
        LOG.debug("Request to get Module : {}", id);
        return moduleRepository.findById(id);
    }

    // ================= Delete =================
    @CacheEvict(cacheNames = ModuleRepository.MODULES, key = "'all'")
    public void delete(Long id) {
        LOG.debug("Request to delete Module : {}", id);
        moduleRepository.deleteById(id);
    }

    // ================= Extra =================
    public boolean existsByName(String name) {
        return moduleRepository.existsByName(name);
    }
}
