package com.dazzle.asklepios.repository;
import com.dazzle.asklepios.domain.Service;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    Page <Service> findAll(Pageable pageable);
    Page <Service> findByCategory(ServiceCategory category, Pageable pageable);
    Page <Service> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page <Service> findByCodeContainingIgnoreCase(String code, Pageable pageable);

    boolean existsByNameIgnoreCase(String name);
}
