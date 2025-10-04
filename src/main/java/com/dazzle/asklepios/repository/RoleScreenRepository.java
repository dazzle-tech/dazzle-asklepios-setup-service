package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.RoleScreen;
import com.dazzle.asklepios.domain.RoleScreenId;
import com.dazzle.asklepios.domain.enumeration.Operation;
import com.dazzle.asklepios.domain.enumeration.Screen;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleScreenRepository extends JpaRepository<RoleScreen, RoleScreenId> {

    void deleteByIdRoleId(Long roleId);
    List<RoleScreen> findByIdRoleId(Long roleId);

}

