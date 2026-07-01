package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.User;
import com.dazzle.asklepios.domain.UserBookableDepartment;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.UserBookableDepartmentRepository;
import com.dazzle.asklepios.repository.UserRepository;
import com.dazzle.asklepios.service.dto.userBookableDepartment.UserBookableDepartmentCreateVM;
import com.dazzle.asklepios.web.rest.vm.userBookableDepartments.UserBookableDepartmentResponseVM;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UserBookableDepartmentService {

    private static final String ENTITY_NAME = "UserBookableDepartment";
    private static final Logger LOG = LoggerFactory.getLogger(UserBookableDepartmentService.class);

    private final UserBookableDepartmentRepository userBookableDepartmentRepository;
    private final UserRepository userRepository;
    private final DepartmentsRepository departmentRepository;


    public UserBookableDepartment createUserBookableDepartment(UserBookableDepartmentCreateVM vm) {
        LOG.debug("Create UFD request vm={}", vm);
        Long userId = vm.userId();
        Long departmentId = vm.departmentId();

        if (userBookableDepartmentRepository.existsByUserIdAndDepartmentId(userId, departmentId)) {
            throw new BadRequestAlertException("departmentexists", ENTITY_NAME, "User already has this department");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestAlertException("notfound", ENTITY_NAME, "User not found"));
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new BadRequestAlertException("notfound", ENTITY_NAME, "Department not found"));

        UserBookableDepartment ufd = UserBookableDepartment.builder()
                .user(user)
                .department(department)
                .build();

        return userBookableDepartmentRepository.save(ufd);
    }

    @Transactional(readOnly = true)
    public boolean exists(Long userId, Long departmentId) {
        return userBookableDepartmentRepository.findByUserIdAndDepartmentId(userId, departmentId).isPresent();
    }

    @Transactional
    public void hardDelete(Long id) {
        userBookableDepartmentRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException("notfound", ENTITY_NAME, "Mapping not found"));

        userBookableDepartmentRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<UserBookableDepartmentResponseVM> getUserBookableDepartmentsByUserInFacility(Long userId, Long facilityId) {
        LOG.debug("List active user departments by userId={} facilityId={}", userId, facilityId);
        return userBookableDepartmentRepository.findByUserIdAndDepartment_FacilityId(userId, facilityId)
                .stream()
                .map(UserBookableDepartmentResponseVM::ofEntity)
                .toList();
    }


}
