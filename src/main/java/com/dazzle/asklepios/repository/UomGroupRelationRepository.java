package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.UomGroupsRelation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UomGroupRelationRepository extends JpaRepository<UomGroupsRelation, Long> {
    List<UomGroupsRelation> findByGroup_Id(Long groupId);
}
