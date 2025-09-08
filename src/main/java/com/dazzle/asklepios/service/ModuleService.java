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


}
