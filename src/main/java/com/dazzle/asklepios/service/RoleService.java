package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.Role;
import com.dazzle.asklepios.repository.RoleRepository;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.web.rest.vm.RoleCreateVM;
import com.dazzle.asklepios.web.rest.vm.RoleUpdateVM;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

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

    public Role create(RoleCreateVM roleVM) {
        LOG.debug("Request to create Role : {}", roleVM);

        Facility facility = facilityRepository.findById(roleVM.facilityId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Facility not found with id " + roleVM.facilityId()
                ));

        Role role = Role.builder()
                .name(roleVM.name())
                .type(roleVM.type())
                .facility(facility)
                .build();

        return roleRepository.save(role);
    }

    public Optional<Role> update(Long id, RoleUpdateVM roleVM) {
        LOG.debug("Request to update Role id={} with data: {}", id, roleVM);

        return roleRepository.findById(id).map(existing -> {
            existing.setName(roleVM.name());
            existing.setType(roleVM.type());

            if (roleVM.facilityId() != null) {
                Facility facility = facilityRepository.findById(roleVM.facilityId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Facility not found with id " + roleVM.facilityId()
                        ));
                existing.setFacility(facility);
            }

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

    @Transactional(readOnly = true)
    public List<Role> findByFacilityId(Long facilityId) {
        LOG.debug("Request to get Roles by Facility id={}", facilityId);
        return roleRepository.findByFacilityId(facilityId);
    }

    public boolean delete(Long id) {
        LOG.debug("Request to delete Role : {}", id);
        if (!roleRepository.existsById(id)) {
            return false;
        }
        roleRepository.deleteById(id);
        return true;
    }
}
