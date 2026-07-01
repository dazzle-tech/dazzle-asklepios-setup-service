package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.User;
import com.dazzle.asklepios.domain.UserDepartment;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.UserDepartmentRepository;
import com.dazzle.asklepios.repository.UserRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.userDepartments.UserDepartmentCreateVM;
import com.dazzle.asklepios.web.rest.vm.userDepartments.UserDepartmentResponseVM;
import com.dazzle.asklepios.web.rest.vm.userDepartments.UserDepartmentTogglesVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
            throw new BadRequestAlertException("departmentexists", ENTITY_NAME, "User already has this department");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestAlertException("notfound", ENTITY_NAME, "User not found"));
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new BadRequestAlertException("notfound", ENTITY_NAME, "Department not found"));

        Long facilityId = department.getFacility().getId(); // or department.getFacilityId()

        if (wantDefault) {
            boolean defaultInFacility =
                    userDepartmentRepository.existsByUserIdAndIsDefaultTrueAndDepartment_Facility_Id(userId, facilityId);
            if (defaultInFacility) {
                throw new BadRequestAlertException("defaultexists", ENTITY_NAME, "User already has a default for this facility");
            }
        }

        boolean isActive = vm.isActive() != null ? vm.isActive() : true;

        UserDepartment ufd = UserDepartment.builder()
                .user(user)
                .department(department)
                .isActive(isActive)
                .isDefault(wantDefault)
                .appointmentBookingAllowed(Boolean.TRUE.equals(vm.appointmentBookingAllowed()))
                .build();

        return userDepartmentRepository.save(ufd);
    }


    @Transactional(readOnly = true)
    public List<UserDepartmentResponseVM> getUserDepartmentsByUser(Long userId) {
        return userDepartmentRepository.findByUserId(userId)
                .stream()
                .map(UserDepartmentResponseVM::ofEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(Long userId, Long departmentId) {
        return userDepartmentRepository.findByUserIdAndDepartmentId(userId, departmentId).isPresent();
    }

    @Transactional
    public void hardDelete(Long id) {
        UserDepartment target = userDepartmentRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException("notfound", ENTITY_NAME, "Mapping not found"));

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
    public List<UserDepartmentResponseVM> getActiveUserDepartmentsByUserInFacility(Long userId, Long facilityId) {
        LOG.debug("List active user departments by userId={} facilityId={}", userId, facilityId);
        return userDepartmentRepository.findByUserIdAndDepartment_FacilityIdAndIsActiveTrueOrderByIsDefaultDescIdAsc(userId, facilityId)
                .stream()
                .map(UserDepartmentResponseVM::ofEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<UserDepartment> getDefaultUserDepartmentByFacility(Long userId, Long facilityId) {
        return userDepartmentRepository.findFirstByUserIdAndDepartment_Facility_IdAndIsDefaultTrue(userId, facilityId);
    }

    @Transactional(readOnly = true)
    public String getFullNameByLogin(String login) {
        LOG.debug("Request to get full name by login={}", login);
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new BadRequestAlertException("notfound", ENTITY_NAME, "User not found"));

        String firstName = user.getFirstName() != null ? user.getFirstName().trim() : "";
        String lastName = user.getLastName() != null ? user.getLastName().trim() : "";
        String fullName = (firstName + " " + lastName).trim();

        LOG.debug("Resolved full name for login={} -> {}", login, fullName.isEmpty() ? user.getLogin() : fullName);
        return fullName.isEmpty() ? user.getLogin() : fullName;
    }
    @Transactional(readOnly = true)
    public Long getIdByLogin(String login) {
        LOG.debug("Request to get id by login={}", login);
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new BadRequestAlertException("notfound", ENTITY_NAME, "User not found"));

        return user.getId();
    }

    @Transactional
    public UserDepartment updateToggles(Long id, UserDepartmentTogglesVM userDepartmentTogglesVM) {
        LOG.debug("Update User Department request userDepartmentTogglesVM={}", userDepartmentTogglesVM);
        UserDepartment userDepartment = userDepartmentRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException("notfound", ENTITY_NAME, "UserDepartment not found"));

        Long userId = userDepartment.getUser().getId();
        Long facilityId = userDepartment.getDepartment().getFacility().getId();
        boolean wantDefault = Boolean.TRUE.equals(userDepartmentTogglesVM.isDefault());

        // If setting as default, make sure no other default exists in same facility
        if (wantDefault && !Boolean.TRUE.equals(userDepartment.getIsDefault())) {
            boolean defaultInFacility =
                    userDepartmentRepository.existsByUserIdAndIsDefaultTrueAndDepartment_Facility_Id(userId, facilityId);
            if (defaultInFacility) {
                throw new BadRequestAlertException("defaultexists", ENTITY_NAME, "User already has a default for this facility");
            }
        }

        userDepartment.setIsDefault(wantDefault);
        userDepartment.setAppointmentBookingAllowed(Boolean.TRUE.equals(userDepartmentTogglesVM.appointmentBookingAllowed()));

        return userDepartmentRepository.save(userDepartment);
    }
    @Transactional(readOnly = true)
    public List<User> getUserDepartmentsForDepartment(Long departmentId) {
        LOG.debug("Request to get Users linked to Department id={}", departmentId);
        return userDepartmentRepository
                .findAllByDepartment_IdAndIsActiveTrue(departmentId)
                .stream()
                .map(UserDepartment::getUser)
                .filter(Objects::nonNull)
                .toList();
    }
}
