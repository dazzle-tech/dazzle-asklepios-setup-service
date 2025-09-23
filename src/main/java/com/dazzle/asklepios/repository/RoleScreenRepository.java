package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.RoleScreen;
import com.dazzle.asklepios.domain.enumeration.Operation;
import com.dazzle.asklepios.domain.enumeration.Screen;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;

public interface RoleScreenRepository extends JpaRepository<RoleScreen, Long> {

    List<RoleScreen> findByRoleId(Long roleId);

    Optional<RoleScreen> findByRoleIdAndScreenAndOperation(Long roleId, Screen screen, Operation operation);


    @Modifying
    @Transactional
    void deleteByRoleId(Long roleId);

    void deleteByRoleIdAndScreenAndOperation(Long roleId, Screen screen, Operation operation);
}
