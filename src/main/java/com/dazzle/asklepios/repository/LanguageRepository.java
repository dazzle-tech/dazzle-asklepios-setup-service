package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LanguageRepository extends JpaRepository<Language, Long> {

    Optional<Language> findByLangKey(String langKey);

    boolean existsByLangKey(String langKey);
}
