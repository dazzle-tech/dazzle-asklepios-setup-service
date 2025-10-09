package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.web.rest.vm.FacilityCreateVM;
import com.dazzle.asklepios.web.rest.vm.FacilityUpdateVM;
import com.dazzle.asklepios.web.rest.vm.FacilityResponseVM;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FacilityService {

    private static final Logger LOG = LoggerFactory.getLogger(FacilityService.class);

    private final FacilityRepository facilityRepository;

    public FacilityService(FacilityRepository facilityRepository) {
        this.facilityRepository = facilityRepository;
    }

     @CacheEvict(cacheNames = FacilityRepository.FACILITIES, key = "'all'")
    public FacilityResponseVM create(FacilityCreateVM vm) {
        LOG.debug("Request to create Facility : {}", vm);

        Facility facility = new Facility();
        facility.setName(vm.name());
        facility.setType(vm.type());
        facility.setCode(vm.code());
        facility.setEmailAddress(vm.emailAddress());
        facility.setPhone1(vm.phone1());
        facility.setPhone2(vm.phone2());
        facility.setFax(vm.fax());
        facility.setAddressId(vm.addressId());
        facility.setDefaultCurrency(vm.defaultCurrency());

        Facility saved = facilityRepository.save(facility);
        return FacilityResponseVM.ofEntity(saved);
    }


    @CacheEvict(cacheNames = FacilityRepository.FACILITIES, key = "'all'")
    public Optional<Facility> update(Long id, FacilityUpdateVM vm) {
        LOG.debug("Request to update Facility id={} with {}", id, vm);

        return facilityRepository.findById(id).map(existing -> {
            if (vm.name() != null) existing.setName(vm.name());
            if (vm.type() != null) existing.setType(vm.type());
            if (vm.emailAddress() != null) existing.setEmailAddress(vm.emailAddress());
            if (vm.phone1() != null) existing.setPhone1(vm.phone1());
            if (vm.phone2() != null) existing.setPhone2(vm.phone2());
            if (vm.fax() != null) existing.setFax(vm.fax());
            if (vm.addressId() != null) existing.setAddressId(vm.addressId());
            if (vm.defaultCurrency() != null) existing.setDefaultCurrency(vm.defaultCurrency());

            Facility updated = facilityRepository.save(existing);
            return updated;
        });
    }

    @Transactional(readOnly = true)
    public List<FacilityResponseVM> findAll() {
        LOG.debug("Request to get all Facilities");
        return facilityRepository.findAll()
                .stream()
                .map(FacilityResponseVM::ofEntity)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public Optional<FacilityResponseVM> findOne(Long id) {
        LOG.debug("Request to get Facility : {}", id);
        return facilityRepository.findById(id)
                .map(FacilityResponseVM::ofEntity);
    }

     @CacheEvict(cacheNames = FacilityRepository.FACILITIES, key = "'all'")
    public boolean delete(Long id) {
        LOG.debug("Request to delete Facility : {}", id);
        if (!facilityRepository.existsById(id)) {
            return false;
        }
        facilityRepository.deleteById(id);
        return true;
    }
}
