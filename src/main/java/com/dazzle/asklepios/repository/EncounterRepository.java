package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Encounter;
import com.dazzle.asklepios.domain.enumeration.Resource;
import com.dazzle.asklepios.domain.enumeration.Status;
import com.dazzle.asklepios.domain.enumeration.Visit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EncounterRepository extends JpaRepository<Encounter, Long> {

    @Override
    @EntityGraph(attributePaths = "patient")
    Page<Encounter> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "patient")
    Page<Encounter> findByStatus(Status status, Pageable pageable);

    @EntityGraph(attributePaths = "patient")
    Page<Encounter> findByPatient_Id(Long patientId, Pageable pageable);

    @EntityGraph(attributePaths = "patient")
    Page<Encounter> findByResourceType(Resource resource, Pageable pageable);

    @EntityGraph(attributePaths = "patient")
    Page<Encounter> findByVisitType(Visit visit, Pageable pageable);

    // (اختياري) جلب واحد مع patient إن احتجته
    @EntityGraph(attributePaths = "patient")
    Optional<Encounter> findById(Long id);
}
