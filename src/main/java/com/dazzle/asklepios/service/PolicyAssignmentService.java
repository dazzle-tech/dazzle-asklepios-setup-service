package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.PolicyAssignment;
import com.dazzle.asklepios.domain.PolicyDefinition;
import com.dazzle.asklepios.domain.enumeration.PolicyResourceType;
import com.dazzle.asklepios.repository.CatalogRepository;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.DiagnosticTestRepository;
import com.dazzle.asklepios.repository.PolicyAssignmentRepository;
import com.dazzle.asklepios.repository.PolicyDefinitionRepository;
import com.dazzle.asklepios.repository.PractitionersRepository;
import com.dazzle.asklepios.repository.RoomRepository;
import com.dazzle.asklepios.repository.ServiceRepository;
import com.dazzle.asklepios.service.dto.policyAssignment.PolicyAssignmentCreateDTO;
import com.dazzle.asklepios.service.dto.policyAssignment.PolicyAssignmentUpdateDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class PolicyAssignmentService {

    private static final Logger LOG = LoggerFactory.getLogger(PolicyAssignmentService.class);

    private static final String ENTITY_NAME = "policyAssignment";

    private final PolicyAssignmentRepository policyAssignmentRepository;
    private final PolicyDefinitionRepository policyDefinitionRepository;
    private final DepartmentsRepository departmentsRepository;
    private final PractitionersRepository practitionersRepository;
    private final CatalogRepository catalogRepository;
    private final ServiceRepository serviceRepository;
    private final DiagnosticTestRepository diagnosticTestRepository;
    private final RoomRepository roomRepository;

    public PolicyAssignmentService(PolicyAssignmentRepository policyAssignmentRepository, PolicyDefinitionRepository policyDefinitionRepository, DepartmentsRepository departmentsRepository, PractitionersRepository practitionersRepository, CatalogRepository catalogRepository, ServiceRepository serviceRepository, DiagnosticTestRepository diagnosticTestRepository, RoomRepository roomRepository) {
        this.policyAssignmentRepository = policyAssignmentRepository;
        this.policyDefinitionRepository = policyDefinitionRepository;
        this.departmentsRepository = departmentsRepository;
        this.practitionersRepository = practitionersRepository;
        this.catalogRepository = catalogRepository;
        this.serviceRepository = serviceRepository;
        this.diagnosticTestRepository = diagnosticTestRepository;
        this.roomRepository = roomRepository;
    }

    public PolicyAssignment create(PolicyAssignmentCreateDTO dto) {
        LOG.debug("Request to create PolicyAssignment: {}", dto);

        PolicyDefinition policy = getPolicy(dto.policyId());

        validateResourceExists(dto.resourceType(), dto.resourceId());

        validateDuplicate(dto.policyId(), dto.resourceType(), dto.resourceId());

        PolicyAssignment assignment = PolicyAssignment.builder()
                .policy(policy)
                .facility(policy.getFacility())
                .resourceType(dto.resourceType())
                .resourceId(dto.resourceId())
                .isActive(true)
                .isRequired(dto.isRequired() != null ? dto.isRequired() : true)
                .build();

        return policyAssignmentRepository.save(assignment);
    }

    public PolicyAssignment update(PolicyAssignmentUpdateDTO dto) {
        LOG.debug("Request to update PolicyAssignment isRequired: {}", dto);

        PolicyAssignment assignment = getById(dto.id());

        assignment.setIsRequired(dto.isRequired());

        return policyAssignmentRepository.save(assignment);
    }

    @Transactional(readOnly = true)
    public PolicyAssignment getOne(Long id) {
        LOG.debug("Request to get PolicyAssignment by id: {}", id);
        return getById(id);
    }

    @Transactional(readOnly = true)
    public List<PolicyAssignment> getAll() {
        LOG.debug("Request to get all PolicyAssignments");
        return policyAssignmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<PolicyAssignment> getAllByResource(PolicyResourceType resourceType, Long resourceId) {
        LOG.debug("Request to get PolicyAssignments by resourceType={}, resourceId={}", resourceType, resourceId);

        return policyAssignmentRepository.findAllByResourceTypeAndResourceId(resourceType, resourceId);
    }

    @Transactional(readOnly = true)
    public List<PolicyAssignment> getActiveByFacilityAndResource(Long facilityId, PolicyResourceType resourceType, Long resourceId) {
        LOG.debug("Request to get active PolicyAssignments by facilityId={}, resourceType={}, resourceId={}", facilityId, resourceType, resourceId);

        return policyAssignmentRepository.findAllByFacilityIdAndResourceTypeAndResourceIdAndIsActiveTrue(facilityId, resourceType, resourceId);
    }

    public PolicyAssignment toggleActive(Long id) {
        LOG.debug("Request to toggle active PolicyAssignment id={}", id);

        PolicyAssignment assignment = getById(id);

        Boolean currentValue = Boolean.TRUE.equals(assignment.getIsActive());
        assignment.setIsActive(!currentValue);

        return policyAssignmentRepository.save(assignment);
    }

    @Transactional(readOnly = true)
    public List<PolicyAssignment> getEffectiveActivePolicyAssignments(Long facilityId, Long departmentId, PolicyResourceType resourceType, Long resourceId) {
        LOG.debug("Get effective active policy assignments facilityId={}, departmentId={}, resourceType={}, resourceId={}", facilityId, departmentId, resourceType, resourceId);

        if (resourceType == PolicyResourceType.DEPARTMENT) {
            return policyAssignmentRepository.findAllByFacilityIdAndResourceTypeAndResourceIdAndIsActiveTrue(
                    facilityId,
                    PolicyResourceType.DEPARTMENT,
                    departmentId
            );
        }

        List<PolicyAssignment> departmentAssignments = policyAssignmentRepository.findAllByFacilityIdAndResourceTypeAndResourceIdAndIsActiveTrue(facilityId, PolicyResourceType.DEPARTMENT, departmentId);

        List<PolicyAssignment> resourceAssignments = policyAssignmentRepository.findAllByFacilityIdAndResourceTypeAndResourceIdAndIsActiveTrue(facilityId, resourceType, resourceId);

        Map<Long, PolicyAssignment> uniqueByPolicyId = new LinkedHashMap<>();

        for (PolicyAssignment departmentAssignment : departmentAssignments) {
            Long policyId = extractPolicyId(departmentAssignment);

            if (policyId != null) {
                uniqueByPolicyId.put(policyId, departmentAssignment);
            }
        }

        /*
         * Resource-level assignment is more specific.
         * If same policy exists in department and resource, this will override department assignment.
         */
        for (PolicyAssignment resourceAssignment : resourceAssignments) {
            Long policyId = extractPolicyId(resourceAssignment);

            if (policyId != null) {
                uniqueByPolicyId.put(policyId, resourceAssignment);
            }
        }

        return new ArrayList<>(uniqueByPolicyId.values());
    }

    private Long extractPolicyId(PolicyAssignment policyAssignment) {
        if (policyAssignment == null) {
            return null;
        }

        if (policyAssignment.getPolicy() != null) {
            return policyAssignment.getPolicy().getId();
        }

        return policyAssignment.getPolicy().getId();
    }

    private PolicyAssignment getById(Long id) {
        return policyAssignmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException(
                        "policyAssignment.notfound",
                        ENTITY_NAME,
                        "Policy assignment not found with id " + id
                ));
    }

    private PolicyDefinition getPolicy(Long policyId) {
        return policyDefinitionRepository.findById(policyId)
                .orElseThrow(() -> new NotFoundAlertException(
                        "policyDefinition.notfound",
                        "policyDefinition",
                        "Policy definition not found with id " + policyId
                ));
    }

    private void validateDuplicate(Long policyId, PolicyResourceType resourceType, Long resourceId) {
        boolean exists = policyAssignmentRepository.existsByPolicy_IdAndResourceTypeAndResourceId(policyId, resourceType, resourceId);

        if (exists) {
            throw new BadRequestAlertException(
                    "policyAssignment.duplicate",
                    ENTITY_NAME,
                    "Policy assignment already exists for this resource"
            );
        }
    }

    private void validateResourceExists(PolicyResourceType resourceType, Long resourceId) {
        if (resourceType == null) {
            throw new BadRequestAlertException(
                    "resourceType.required",
                    ENTITY_NAME,
                    "Resource type is required"
            );
        }

        if (resourceId == null) {
            throw new BadRequestAlertException(
                    "resourceId.required",
                    ENTITY_NAME,
                    "Resource id is required"
            );
        }

        boolean exists = switch (resourceType) {
            case DEPARTMENT -> departmentsRepository.existsById(resourceId);
            case PRACTITIONER -> practitionersRepository.existsById(resourceId);
            case CATALOG -> catalogRepository.existsById(resourceId);
            case SERVICE -> serviceRepository.existsById(resourceId);
            case DIAGNOSTIC_TEST -> diagnosticTestRepository.existsById(resourceId);
            case ROOM -> roomRepository.existsById(resourceId);
        };

        if (!exists) {
            throw new BadRequestAlertException(
                    "resource.notfound",
                    ENTITY_NAME,
                    resourceType + " not found with id " + resourceId
            );
        }
    }
}
