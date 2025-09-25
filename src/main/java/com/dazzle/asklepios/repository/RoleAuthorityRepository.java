package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.RoleAuthority;
import com.dazzle.asklepios.domain.RoleAuthorityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RoleAuthorityRepository extends JpaRepository<RoleAuthority, RoleAuthorityId> {


    @Query("select ra.id.authorityName from RoleAuthority ra where ra.id.roleId = :roleId")
    List<String> findAuthorityNamesByRoleId(Long roleId);


    List<RoleAuthority> findByIdRoleId(Long roleId);


    void deleteByIdRoleId(Long roleId);


}


