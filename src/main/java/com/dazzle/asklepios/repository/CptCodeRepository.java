package com.dazzle.asklepios.repository;
import com.dazzle.asklepios.domain.CptCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CptCodeRepository extends JpaRepository<CptCode, Long> {
    Optional<CptCode> findByCode(String code);
    Page<CptCode> findByCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String code, String description, Pageable pageable);
    Page<CptCode> findByMainCategoryIgnoreCase(String mainCategory, Pageable pageable);
    Page<CptCode> findByServiceCategoryIgnoreCase(String serviceCategory, Pageable pageable);
    Page<CptCode> findByCodeContainingIgnoreCase(String code, Pageable pageable);
    Page<CptCode> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);
}
