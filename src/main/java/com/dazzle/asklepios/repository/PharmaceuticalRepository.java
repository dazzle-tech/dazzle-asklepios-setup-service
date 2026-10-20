package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Pharmaceutical;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PharmaceuticalRepository extends JpaRepository<Pharmaceutical, Long> {

    @EntityGraph(attributePaths = "activeIngredient")
    List<Pharmaceutical> findByActiveIngredientIdOrderByIdAsc(Long activeIngredientId);

    @EntityGraph(attributePaths = "activeIngredient")
    Optional<Pharmaceutical> findWithActiveIngredientById(Long id);

    @EntityGraph(attributePaths = "activeIngredient")
    List<Pharmaceutical> findWithActiveIngredientByIdIn(Collection<Long> ids);

    @EntityGraph(attributePaths = "activeIngredient")
    @Query("""
            select p from Pharmaceutical p
            where p.isActive = true
              and p.activeIngredient.isActive = true
              and (lower(p.pharmaceuticalName) like lower(concat('%', :search, '%'))
                   or lower(p.activeIngredient.name) like lower(concat('%', :search, '%')))
            order by p.pharmaceuticalName asc
            """)
    List<Pharmaceutical> searchActive(@Param("search") String search);
}
