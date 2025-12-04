package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

    Page<Resource> findAll(Pageable pageable);

    Page<Resource> findByResourceType(String resourceType, Pageable pageable);

    Page<Resource> findByResourceTypeAndIsActiveTrue(String resourceType, Pageable pageable);

    List<Resource> findByResourceTypeAndIsActiveTrue(String resourceType);
}
