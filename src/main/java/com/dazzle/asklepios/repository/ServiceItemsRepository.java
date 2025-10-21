package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.ServiceItems;
import com.dazzle.asklepios.domain.enumeration.ServiceItemsType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceItemsRepository extends JpaRepository<ServiceItems, Long> {
    boolean existsByServiceIdAndTypeAndSourceId(Long serviceId, ServiceItemsType type, Long sourceId);
    boolean existsByServiceIdAndTypeAndSourceIdAndIdNot(Long serviceId, ServiceItemsType type, Long sourceId, Long id);
    Page<ServiceItems> findAll(Pageable pageable);
    Page<ServiceItems> findByServiceId(Long serviceId, Pageable pageable);
    List<ServiceItems> findByServiceId(Long serviceId);

}
