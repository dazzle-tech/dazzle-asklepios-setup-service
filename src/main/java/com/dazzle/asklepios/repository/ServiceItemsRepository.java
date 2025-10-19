package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.ServiceItems;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceItemsRepository extends JpaRepository<ServiceItems, Long> {

    List<ServiceItems> findByServiceId(Long serviceId);
    Page<ServiceItems> findAll(Pageable pageable);
    Page<ServiceItems> findByServiceId(Long serviceId, Pageable pageable);
}
