package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.RoleAuthority;
import com.dazzle.asklepios.domain.RoleAuthorityId;
import com.dazzle.asklepios.domain.enumeration.Operation;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RoleAuthorityRepository extends JpaRepository<RoleAuthority, RoleAuthorityId> {


    @Modifying
    @Query("delete from RoleAuthority ra where ra.id.roleId = :roleId and ra.id.authorityName = :authorityName")
    void deleteByRoleIdAndAuthority(@Param("roleId") Long roleId,
                                    @Param("authorityName") String authorityName);



}


