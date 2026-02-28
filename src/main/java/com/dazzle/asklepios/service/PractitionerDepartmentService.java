package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.Practitioner;
import com.dazzle.asklepios.domain.PractitionerDepartment;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.PractitionerDepartmentRepository;
import com.dazzle.asklepios.repository.PractitionersRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.practitionerDepartment.PractitionerDepartmentCreateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PractitionerDepartmentService {

    private static final String ENTITY_NAME = "practitionerDepartment";
    private static final Logger LOG = LoggerFactory.getLogger(PractitionerDepartmentService.class);
    private final PractitionerDepartmentRepository practitionerDepartmentRepository;
    private final PractitionersRepository practitionerRepo;
    private final DepartmentsRepository departmentRepo;

    public PractitionerDepartmentService(
            PractitionerDepartmentRepository repo,
            PractitionersRepository practitionerRepo,
            DepartmentsRepository departmentRepo) {
        this.practitionerDepartmentRepository = repo;
        this.practitionerRepo = practitionerRepo;
        this.departmentRepo = departmentRepo;
    }

    public PractitionerDepartment create(PractitionerDepartmentCreateVM vm) {
        LOG.debug("Request to link Practitioner {} with Department {}", vm.practitionerId(), vm.departmentId());
        Practitioner practitioner = practitionerRepo.findById(vm.practitionerId())
                .orElseThrow(() -> new BadRequestAlertException("Practitioner not found", ENTITY_NAME, "practitioner.notfound"));
        Department department = departmentRepo.findById(vm.departmentId())
                .orElseThrow(() -> new BadRequestAlertException("Department not found", ENTITY_NAME, "department.notfound"));

        boolean exists = practitionerDepartmentRepository.existsByPractitionerIdAndDepartmentId(vm.practitionerId(), vm.departmentId());
        if (exists) {
            LOG.debug("Relation already exists for practitionerId={} departmentId={}", vm.practitionerId(), vm.departmentId());
            throw new BadRequestAlertException("Relation already exists", ENTITY_NAME, "relation.exists");
        }

        PractitionerDepartment entity = PractitionerDepartment.builder()
                .practitioner(practitioner)
                .department(department)
                .build();


        PractitionerDepartment saved = practitionerDepartmentRepository.save(entity);
        LOG.debug("Created PractitionerDepartment link id={} practitionerId={} departmentId={}",
                saved.getId(), vm.practitionerId(), vm.departmentId());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<PractitionerDepartment> findByPractitionerId(Long practitionerId) {
        LOG.debug("Request to get department links by practitionerId={}", practitionerId);
        List<PractitionerDepartment> links = practitionerDepartmentRepository.findByPractitionerId(practitionerId);
        LOG.debug("Found {} department links for practitionerId={}", links.size(), practitionerId);
        return links;
    }

    @Transactional(readOnly = true)
    public Page<Practitioner> findPractitionersByDepartmentId(Long departmentId, Pageable pageable) {
        LOG.debug("Request to get practitioners by departmentId={} pageable={}", departmentId, pageable);

        if (!departmentRepo.existsById(departmentId)) {
            LOG.debug("Department not found for departmentId={}", departmentId);
            throw new BadRequestAlertException("Department not found", ENTITY_NAME, "department.notfound");
        }

        List<Long> practitionerDepartmentIds = practitionerDepartmentRepository.findByDepartmentId(departmentId).stream()
                .map(practitionerDepartment -> practitionerDepartment.getPractitioner().getId())
                .distinct()
                .toList();
        LOG.debug("Resolved {} unique practitioner practitionerDepartmentIds for departmentId={}", practitionerDepartmentIds.size(), departmentId);

        if (practitionerDepartmentIds.isEmpty()) {
            LOG.debug("No practitioners linked to departmentId={}, returning empty page", departmentId);
            return Page.empty(pageable);
        }

        Page<Practitioner> page = practitionerRepo.findByIdInAndIsActiveTrueAndAppointableTrue(practitionerDepartmentIds, pageable);
        LOG.debug("Found {} practitioners (totalElements={}) for departmentId={}",
                page.getNumberOfElements(), page.getTotalElements(), departmentId);
        return page;
    }

    @Transactional
    public void delete(Long practitionerId, Long departmentId) {
        LOG.debug("Deleting link Practitioner {} – Department {}", practitionerId, departmentId);
        LOG.debug("Request to delete practitioner-department link practitionerId={} departmentId={}",
                practitionerId, departmentId);
        practitionerDepartmentRepository.findByPractitionerId(practitionerId).stream()
                .filter(pd -> departmentId.equals(pd.getDepartment().getId()))
                .findFirst()
                .ifPresentOrElse(
                        pd -> {
                            practitionerDepartmentRepository.delete(pd);
                            LOG.debug("Deleted PractitionerDepartment link id={} practitionerId={} departmentId={}",
                                    pd.getId(), practitionerId, departmentId);
                        },
                        () -> LOG.debug("No PractitionerDepartment link found for practitionerId={} departmentId={}",
                                practitionerId, departmentId)
                );
    }

}
