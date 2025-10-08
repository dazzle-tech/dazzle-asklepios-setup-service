package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.User;
import com.dazzle.asklepios.domain.UserFacilityDepartment;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.repository.UserFacilityDepartmentRepository;
import com.dazzle.asklepios.repository.UserRepository;
import com.dazzle.asklepios.web.rest.vm.UserFacilityDepartmentCreateVM;
import com.dazzle.asklepios.web.rest.vm.UserFacilityDepartmentResponseVM;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;

@Service
@Transactional
public class UserFacilityDepartmentService {

    private static final String ENTITY_NAME = "userFacilityDepartment";
    private static final Logger LOG = LoggerFactory.getLogger(UserFacilityDepartmentService.class);

    private final UserFacilityDepartmentRepository userFacilityDepartmentsRepository;
    private final FacilityRepository facilityRepository;
    private final UserRepository userRepository;
    private final DepartmentsRepository departmentRepository;

    public UserFacilityDepartmentService(UserFacilityDepartmentRepository repository, FacilityRepository facilityRepository, UserRepository userRepository, DepartmentsRepository departmentRepository) {
        this.userFacilityDepartmentsRepository = repository;
        this.facilityRepository = facilityRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    public UserFacilityDepartmentResponseVM createUserFacilityDepartment(UserFacilityDepartmentCreateVM vm) {
        LOG.debug("Create UFD request vm={}", vm);
        Long facilityId = vm.facilityId();
        Long userId = vm.userId();
        Long departmentId = vm.departmentId();

        var existing = userFacilityDepartmentsRepository.findByFacilityIdAndUserIdAndDepartmentId(facilityId, userId, departmentId);
        if (existing.isPresent()) {
            return UserFacilityDepartmentResponseVM.ofEntity(existing.get());
        }

        Facility facilityRef = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new BadRequestAlertException("Facility not found", ENTITY_NAME, "notfound"));
        User userRef = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestAlertException("User not found", ENTITY_NAME, "notfound"));
        Department departmentRef = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new BadRequestAlertException("Department not found", ENTITY_NAME, "notfound"));

        UserFacilityDepartment ufd = UserFacilityDepartment.builder()
                .facility(facilityRef)
                .user(userRef)
                .department(departmentRef)
                .isActive(vm.isActive() != null ? vm.isActive() : Boolean.TRUE)
                .createdBy(vm.createdBy())
                .createdDate(vm.createdDate() != null ? vm.createdDate() : Instant.now())
                .build();

        return UserFacilityDepartmentResponseVM.ofEntity(userFacilityDepartmentsRepository.save(ufd));
    }

    public void toggleActiveStatus(Long id) {
        LOG.debug("Toggle UFD isActive id={}", id);
        userFacilityDepartmentsRepository.findById(id).ifPresent(ufd -> {
            ufd.setIsActive(!Boolean.TRUE.equals(ufd.getIsActive()));
            ufd.setLastModifiedDate(Instant.now());
            userFacilityDepartmentsRepository.save(ufd);
        });
    }

    @Transactional(readOnly = true)
    public List<UserFacilityDepartmentResponseVM> getUserFacilityDepartmentsByUser(Long userId) {
        LOG.debug("List UFDs by userId={}", userId);
        return userFacilityDepartmentsRepository.findByUserId(userId)
                .stream()
                .map(UserFacilityDepartmentResponseVM::ofEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(Long facilityId, Long userId, Long departmentId) {
        LOG.debug("Check UFD exists facilityId={} userId={} departmentId={}", facilityId, userId, departmentId);
        return userFacilityDepartmentsRepository.findByFacilityIdAndUserIdAndDepartmentId(facilityId, userId, departmentId).isPresent();
    }
}
