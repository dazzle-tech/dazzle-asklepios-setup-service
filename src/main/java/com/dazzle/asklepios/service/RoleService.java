package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.Role;
import com.dazzle.asklepios.repository.RoleRepository;
import com.dazzle.asklepios.repository.FacilityRepository;

import java.util.List;
import java.util.Optional;

import com.dazzle.asklepios.web.rest.vm.RoleVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RoleService {

    private static final Logger LOG = LoggerFactory.getLogger(RoleService.class);

    private final RoleRepository roleRepository;
    private final FacilityRepository facilityRepository;

    public RoleService(RoleRepository roleRepository, FacilityRepository facilityRepository) {
        this.roleRepository = roleRepository;
        this.facilityRepository = facilityRepository;
    }

    public Role create(Role role) {
        LOG.debug("Request to create Role : {}", role);

        role.setId(null);

        Facility facility = facilityRepository.findById(role.getFacilityId())
                .orElseThrow(() -> new IllegalArgumentException("Facility not found with id " + role.getFacilityId()));
        role.setFacility(facility);

        return roleRepository.save(role);
    }


    public Optional<Role> update(Long id, RoleVM roleVM) {
        return roleRepository.findById(id).map(existing -> {
            existing.setName(roleVM.getName());
            existing.setType(roleVM.getType());

            Facility facility = facilityRepository.findById(roleVM.getFacilityId())
                    .orElseThrow(() -> new RuntimeException("Facility not found"));
            existing.setFacility(facility);

            return roleRepository.save(existing);
        });
    }

    @Transactional(readOnly = true)
    public List<Role> findAll() {
        LOG.debug("Request to get all Roles");
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Role> findOne(Long id) {
        LOG.debug("Request to get Role : {}", id);
        return roleRepository.findById(id);
    }

    public void delete(Long id) {
        LOG.debug("Request to delete Role : {}", id);
        roleRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Role> findByFacilityId(Long facilityId) {
        LOG.debug("Request to get Roles by Facility id={}", facilityId);
        return roleRepository.findByFacilityId(facilityId);
    }
}
