package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PolicyAssignment;
import com.dazzle.asklepios.domain.enumeration.PolicyResourceType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyAssignmentRepository extends JpaRepository<PolicyAssignment, Long> {

    List<PolicyAssignment> findAllByResourceTypeAndResourceId(
            PolicyResourceType resourceType,
            Long resourceId
    );


    boolean existsByPolicy_IdAndResourceTypeAndResourceId(
            Long policyId,
            PolicyResourceType resourceType,
            Long resourceId
    );


    List<PolicyAssignment> findAllByFacilityIdAndResourceTypeAndResourceIdAndIsActiveTrue(
            Long facilityId,
            PolicyResourceType resourceType,
            Long resourceId
    );

}