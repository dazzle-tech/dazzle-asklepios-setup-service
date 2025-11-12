package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.User;
import com.dazzle.asklepios.domain.UserDepartment;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.UserDepartmentRepository;
import com.dazzle.asklepios.repository.UserRepository;
import com.dazzle.asklepios.web.rest.vm.userDepartments.UserDepartmentCreateVM;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class UserDepartmentService {

    private static final String ENTITY_NAME = "UserDepartment";
    private static final Logger LOG = LoggerFactory.getLogger(UserDepartmentService.class);

    private final UserDepartmentRepository userDepartmentRepository;
    private final UserRepository userRepository;
    private final DepartmentsRepository departmentRepository;

    public UserDepartmentService(
            UserDepartmentRepository userDepartmentRepository,
            UserRepository userRepository,
            DepartmentsRepository departmentRepository
    ) {
        this.userDepartmentRepository = userDepartmentRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    public UserDepartment createUserDepartment(UserDepartmentCreateVM vm) {
        LOG.debug("Create UFD request vm={}", vm);
        Long userId = vm.userId();
        Long departmentId = vm.departmentId();
        boolean wantDefault = Boolean.TRUE.equals(vm.isDefault());

        if (userDepartmentRepository.existsByUserIdAndDepartmentId(userId, departmentId)) {
            throw new BadRequestAlertException("User already has this department", ENTITY_NAME, "departmentexists");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestAlertException("User not found", ENTITY_NAME, "notfound"));
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new BadRequestAlertException("Department not found", ENTITY_NAME, "notfound"));

        Long facilityId = department.getFacility().getId(); // or department.getFacilityId()

        if (wantDefault) {
            boolean defaultInFacility =
                    userDepartmentRepository.existsByUserIdAndIsDefaultTrueAndDepartment_Facility_Id(userId, facilityId);
            if (defaultInFacility) {
                throw new BadRequestAlertException("User already has a default for this facility", ENTITY_NAME, "defaultexists");
            }
        }

        boolean isActive = vm.isActive() != null ? vm.isActive() : true;

        UserDepartment ufd = UserDepartment.builder()
                .user(user)
                .department(department)
                .isActive(isActive)
                .isDefault(wantDefault)
                .build();

        return userDepartmentRepository.save(ufd);
    }


    @Transactional(readOnly = true)
    public List<UserDepartment> getUserDepartmentsByUser(Long userId) {
        LOG.debug("List UFDs by userId={}", userId);
        return userDepartmentRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public boolean exists(Long userId, Long departmentId) {
        return userDepartmentRepository.findByUserIdAndDepartmentId(userId, departmentId).isPresent();
    }

    @Transactional
    public void hardDelete(Long id) {
        UserDepartment target = userDepartmentRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException("Mapping not found", ENTITY_NAME, "notfound"));

        Long userId = target.getUser().getId();
        Long facilityId = target.getDepartment().getFacility().getId();
        boolean wasActiveDefault = Boolean.TRUE.equals(target.getIsDefault()) && Boolean.TRUE.equals(target.getIsActive());

        userDepartmentRepository.deleteById(id);

        if (wasActiveDefault) {
            Optional<UserDepartment> replacement =
                    userDepartmentRepository.findFirstByUserIdAndDepartment_Facility_IdAndIsActiveTrueOrderByIdAsc(userId, facilityId);
            replacement.ifPresent(ud -> {
                userDepartmentRepository.clearDefaultForUserActiveInFacility(userId, facilityId);
                ud.setIsDefault(true);
                userDepartmentRepository.save(ud);
            });
        }
    }

    @Transactional(readOnly = true)
    public List<UserDepartment> getActiveUserDepartmentsByUserInFacility(Long userId, Long facilityId) {
        LOG.debug("List active user departments by userId={} facilityId={}", userId, facilityId);
        return userDepartmentRepository.findByUserIdAndFacilityAndIsActiveTrueOrderByIsDefaultDesc(userId, facilityId);
    }

    @Transactional(readOnly = true)
    public Optional<UserDepartment> getDefaultUserDepartmentByFacility(Long userId, Long facilityId) {
        return userDepartmentRepository.findFirstByUserIdAndDepartment_Facility_IdAndIsDefaultTrue(userId, facilityId);
    }

}
