package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Module;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    String MODULES = "modules";

    Optional<Module> findByName(String name);

    boolean existsByName(String name);
}
