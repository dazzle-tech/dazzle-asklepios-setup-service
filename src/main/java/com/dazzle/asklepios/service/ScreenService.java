package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Screen;
import com.dazzle.asklepios.repository.ScreenRepository;

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
public class ScreenService {

    private static final Logger LOG = LoggerFactory.getLogger(ScreenService.class);

    private final ScreenRepository screenRepository;

    public ScreenService(ScreenRepository screenRepository) {
        this.screenRepository = screenRepository;
    }

    // ================= Create =================
    @CacheEvict(cacheNames = ScreenRepository.SCREENS, key = "'all'")
    public Screen create(Screen screen) {
        LOG.debug("Request to create Screen : {}", screen);
        screen.setId(null); // ensure a new entity
        return screenRepository.save(screen);
    }

    // ================= Update =================
    @CacheEvict(cacheNames = ScreenRepository.SCREENS, key = "'all'")
    public Optional<Screen> update(Long id, Screen screen) {
        LOG.debug("Request to update Screen id={} with : {}", id, screen);
        return screenRepository
                .findById(id)
                .map(existing -> {
                    existing.setName(screen.getName());
                    existing.setDescription(screen.getDescription());
                    existing.setModule(screen.getModule());
                    existing.setIconImagePath(screen.getIconImagePath());
                    existing.setViewOrder(screen.getViewOrder());
                    existing.setNavPath(screen.getNavPath());
                    return screenRepository.save(existing);
                });
    }

    // ================= Read =================
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = ScreenRepository.SCREENS, key = "'all'")
    public List<Screen> findAll() {
        LOG.debug("Request to get all Screens");
        return screenRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Screen> findOne(Long id) {
        LOG.debug("Request to get Screen : {}", id);
        return screenRepository.findById(id);
    }

    // ================= Delete =================
    @CacheEvict(cacheNames = ScreenRepository.SCREENS, key = "'all'")
    public void delete(Long id) {
        LOG.debug("Request to delete Screen : {}", id);
        screenRepository.deleteById(id);
    }

    // ================= Extra =================
    public boolean existsByModuleAndName(Long moduleId, String name) {
        return screenRepository.existsByModuleIdAndName(moduleId, name);
    }
}
