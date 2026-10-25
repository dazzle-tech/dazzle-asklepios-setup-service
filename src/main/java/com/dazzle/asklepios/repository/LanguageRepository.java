package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Language;
import com.dazzle.asklepios.domain.enumeration.Direction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LanguageRepository extends JpaRepository<Language, Long> {

    Optional<Language> findByLangKey(String langKey);

    boolean existsByLangKey(String langKey);

    List<Language> findByLangNameContainingIgnoreCase(String langName);

    List<Language> findByLangKeyContainingIgnoreCase(String langKey);

    List<Language> findByDirection(Direction direction);
}
