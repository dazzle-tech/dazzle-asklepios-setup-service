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

        Facility facility = new Facility();
        facility.setId(role.getFacilityId());
        role.setFacility(facility);

        return roleRepository.save(role);
    }


    public Optional<Role> update(Long id, RoleVM roleVM) {
        LOG.debug("Request to update Role id={} with data: {}", id, roleVM);

        return roleRepository.findById(id).map(existing -> {
            existing.setName(roleVM.name());
            existing.setType(roleVM.type());

            Facility facility = new Facility();
            facility.setId(roleVM.facilityId());
            existing.setFacility(facility);

            Role updated = roleRepository.save(existing);
            LOG.debug("Role id={} updated successfully", id);
            return updated;
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


    public boolean delete(Long id) {
        LOG.debug("Request to delete Role : {}", id);
        if (!roleRepository.existsById(id)) {
            return false;
        }
        roleRepository.deleteById(id);
        return true;
    }

    @Transactional(readOnly = true)
    public List<Role> findByFacilityId(Long facilityId) {
        LOG.debug("Request to get Roles by Facility id={}", facilityId);
        return roleRepository.findByFacilityId(facilityId);
    }
}
