package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.RoleScreen;
import com.dazzle.asklepios.domain.RoleScreenId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleScreenRepository extends JpaRepository<RoleScreen, RoleScreenId> {

    void deleteByIdRoleId(Long roleId);
    List<RoleScreen> findByIdRoleId(Long roleId);



}

