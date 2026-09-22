package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.NphiesPayer;
import com.dazzle.asklepios.domain.TpaDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TpaDefinitionRepository extends JpaRepository<TpaDefinition, Long> {

    @Override
    @EntityGraph(attributePaths = {"country", "city"})
    Page<TpaDefinition> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"country", "city", "insuranceCompanies"})
    Optional<TpaDefinition> findById(Long id);

    @EntityGraph(attributePaths = {"country", "city", "insuranceCompanies"})
    List<TpaDefinition> findByIdIn(Collection<Long> ids);

    @EntityGraph(attributePaths = {"country", "city", "insuranceCompanies"})
    List<TpaDefinition> findByInsuranceCompanies_Id(Long nphiesPayerId);

    @EntityGraph(attributePaths = {"insuranceCompanies"})
    List<TpaDefinition> findByInsuranceCompanies_IdIn(Collection<Long> nphiesPayerIds);

    @EntityGraph(attributePaths = {"country", "city"})
    Page<TpaDefinition> findByTpaCodeContainingIgnoreCase(String tpaCode, Pageable pageable);

    @EntityGraph(attributePaths = {"country", "city"})
    Page<TpaDefinition> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<TpaDefinition> findByTpaCodeContainingIgnoreCaseOrNameContainingIgnoreCase(String tpaCode, String name);

    @EntityGraph(attributePaths = {"country", "city"})
    Page<TpaDefinition> findByIsActiveTrue(Pageable pageable);

    @EntityGraph(attributePaths = {"country", "city"})
    List<TpaDefinition> findByIsActiveTrue();

    @EntityGraph(attributePaths = {"country", "city"})
    Page<TpaDefinition> findByIsActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);

    List<TpaDefinition> findByIsActiveTrueAndNameIgnoreCase(String name);

    boolean existsByTpaCodeIgnoreCase(String tpaCode);

    boolean existsByTpaCodeIgnoreCaseAndIdNot(String tpaCode, Long id);

    @Query("""
            SELECT p FROM TpaDefinition t
            JOIN t.insuranceCompanies p
            WHERE t.id = :tpaId
              AND p.isActive = true
              AND (
                    :search IS NULL OR :search = ''
                    OR LOWER(p.nameEn) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(COALESCE(p.nameAr, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(p.nphiesId) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """)
    Page<NphiesPayer> searchLinkedInsuranceCompanies(
            @Param("tpaId") Long tpaId,
            @Param("search") String search,
            Pageable pageable
    );
}
