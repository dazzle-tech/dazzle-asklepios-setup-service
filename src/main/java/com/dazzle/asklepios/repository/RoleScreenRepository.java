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

    // كل الشاشات المرتبطة برول معين
    List<RoleScreen> findByRoleId(Long roleId);

    // البحث عن شاشة معينة + عملية لرول
    Optional<RoleScreen> findByRoleIdAndScreenAndOperation(Long roleId, Screen screen, Operation operation);


    @Modifying
    @Transactional
    void deleteByRoleId(Long roleId);
    // حذف شاشة معينة + عملية لرول
    void deleteByRoleIdAndScreenAndOperation(Long roleId, Screen screen, Operation operation);
}
