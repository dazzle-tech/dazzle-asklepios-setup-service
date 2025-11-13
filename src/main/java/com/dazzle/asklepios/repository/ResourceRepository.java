package com.dazzle.asklepios.repository;


import com.dazzle.asklepios.domain.Resource;
 import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

    Page<Resource> findAll(Pageable pageable);

    Page<Resource> findByResourceType(String resourceType, Pageable pageable);


}
