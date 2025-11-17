package com.dazzle.asklepios.repository;
import com.dazzle.asklepios.domain.ProcedureCoding;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcedureCodingRepository extends JpaRepository<ProcedureCoding, Long> {
    Page<ProcedureCoding> findByProcedureId(Long procedureId, Pageable pageable);
}
