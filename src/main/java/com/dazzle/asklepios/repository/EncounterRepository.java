package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Encounter;
import com.dazzle.asklepios.domain.enumeration.Resource;
import com.dazzle.asklepios.domain.enumeration.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EncounterRepository extends JpaRepository<Encounter, Long> {

    List<Encounter> findByStatus(Status status);
    List<Encounter> findByPatient_Id(Long patientId);
    List<Encounter> findByResourceType(Resource resource);
}
