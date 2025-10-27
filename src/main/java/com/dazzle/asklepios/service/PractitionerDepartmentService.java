package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.Practitioner;
import com.dazzle.asklepios.domain.PractitionerDepartment;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.PractitionerDepartmentRepository;
import com.dazzle.asklepios.repository.PractitionersRepository;
import com.dazzle.asklepios.web.rest.vm.practitionerDepartment.PractitionerDepartmentCreateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class PractitionerDepartmentService {

    private static final Logger LOG = LoggerFactory.getLogger(PractitionerDepartmentService.class);
    private final PractitionerDepartmentRepository repo;
    private final PractitionersRepository practitionerRepo;
    private final DepartmentsRepository departmentRepo;

    public PractitionerDepartmentService(
            PractitionerDepartmentRepository repo,
            PractitionersRepository practitionerRepo,
            DepartmentsRepository departmentRepo) {
        this.repo = repo;
        this.practitionerRepo = practitionerRepo;
        this.departmentRepo = departmentRepo;
    }

    public PractitionerDepartment create(PractitionerDepartmentCreateVM vm) {
        LOG.debug("Request to link Practitioner {} with Department {}", vm.practitionerId(), vm.departmentId());
        Practitioner practitioner = practitionerRepo.findById(vm.practitionerId())
                .orElseThrow(() -> new IllegalArgumentException("Practitioner not found"));
        Department department = departmentRepo.findById(vm.departmentId())
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));

        boolean exists = repo.existsByPractitionerIdAndDepartmentId(vm.practitionerId(), vm.departmentId());
        if (exists)
            throw new IllegalStateException("Relation already exists");

        PractitionerDepartment entity = PractitionerDepartment.builder()
                .practitioner(practitioner)
                .department(department)
                .build();

        return repo.save(entity);
    }

    @Transactional(readOnly = true)
    public List<PractitionerDepartment> findByPractitionerId(Long practitionerId) {
        return repo.findByPractitionerId(practitionerId);
    }

    @Transactional
    public void delete(Long practitionerId, Long departmentId) {
        LOG.debug("Deleting link Practitioner {} – Department {}", practitionerId, departmentId);
        repo.findByPractitionerId(practitionerId).stream()
                .filter(pd -> departmentId.equals(pd.getDepartment().getId()))
                .findFirst()
                .ifPresent(repo::delete);
    }

}
