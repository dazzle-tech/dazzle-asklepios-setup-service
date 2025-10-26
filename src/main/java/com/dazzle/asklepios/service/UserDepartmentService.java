package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.User;
import com.dazzle.asklepios.domain.UserDepartment;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.UserDepartmentRepository;
import com.dazzle.asklepios.repository.UserRepository;
import com.dazzle.asklepios.web.rest.vm.UserDepartmentCreateVM;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import java.util.List;
import java.util.Optional;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

@Service
@Transactional
public class UserDepartmentService {

    private static final String ENTITY_NAME = "UserDepartment";
    private static final Logger LOG = LoggerFactory.getLogger(UserDepartmentService.class);
    private final UserDepartmentRepository UserDepartmentsRepository;
    private final UserRepository userRepository;
    private final DepartmentsRepository departmentRepository;
    private final UserDepartmentRepository userDepartmentRepository;

    public UserDepartmentService(UserDepartmentRepository repository, UserRepository userRepository, DepartmentsRepository departmentRepository, UserDepartmentRepository userDepartmentRepository) {
        this.UserDepartmentsRepository = repository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.userDepartmentRepository = userDepartmentRepository;
    }

    public UserDepartment createUserDepartment(UserDepartmentCreateVM vm) {
        LOG.debug("Create UFD request vm={}", vm);
        Long userId = vm.userId();
        Long departmentId = vm.departmentId();

        Optional<UserDepartment> existing = UserDepartmentsRepository.findByUserIdAndDepartmentId(userId, departmentId);
        if (existing.isPresent()) {
            LOG.debug("Existing UFD  exist={}", existing.get());
            return existing.get();
        }

        User userRef = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestAlertException("User not found", ENTITY_NAME, "notfound"));
        Department departmentRef = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new BadRequestAlertException("Department not found", ENTITY_NAME, "notfound"));

        UserDepartment ufd = UserDepartment.builder()
                .user(userRef)
                .department(departmentRef)
                .isActive(vm.isActive() != null ? vm.isActive() : Boolean.TRUE)
                .build();

        return UserDepartmentsRepository.save(ufd);
    }

    @Transactional(readOnly = true)
    public List<UserDepartment> getUserDepartmentsByUser(Long userId) {
        LOG.debug("List UFDs by userId={}", userId);
        return UserDepartmentsRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public boolean exists(Long userId, Long departmentId) {
        LOG.debug("Check UFD exists  userId={} departmentId={}", userId, departmentId);
        return UserDepartmentsRepository.findByUserIdAndDepartmentId(userId, departmentId).isPresent();
    }
    @Transactional
    public void hardDelete(Long id) {
        userDepartmentRepository.deleteById(id);
    }
}