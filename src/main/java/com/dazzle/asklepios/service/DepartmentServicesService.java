package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.DepartmentServices;
import com.dazzle.asklepios.domain.enumeration.patient.EncounterReason;
import com.dazzle.asklepios.repository.DepartmentServicesRepository;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DepartmentServicesService {

    private static final Logger LOG = LoggerFactory.getLogger(DepartmentServicesService.class);

    private final DepartmentServicesRepository repository;
    private final DepartmentsRepository departmentRepository;

    public DepartmentServicesService(
            DepartmentServicesRepository repository,
            DepartmentsRepository departmentRepository
    ) {
        this.repository = repository;
        this.departmentRepository = departmentRepository;
    }

    public List<DepartmentServices> addServices(Long departmentId, List<EncounterReason> services) {
        LOG.debug("Request to add services={} to departmentId={}", services, departmentId);

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Department not found with id " + departmentId,
                        "departmentServices",
                        "department.notfound"
                ));

        return services.stream()
                .filter(service -> !repository.existsByDepartment_IdAndService(departmentId, service))
                .map(service -> DepartmentServices.builder()
                        .department(department)
                        .service(service)
                        .build())
                .map(repository::save)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DepartmentServices> findByDepartment(Long departmentId) {
        LOG.debug("Request to get services for departmentId={}", departmentId);
        return repository.findByDepartment_Id(departmentId);
    }


    public List<DepartmentServices> replaceServices(Long departmentId, List<EncounterReason> services) {
        LOG.debug("Request to replace services for departmentId={} with {}", departmentId, services);

        repository.deleteByDepartment_Id(departmentId);
        return addServices(departmentId, services);
    }
}