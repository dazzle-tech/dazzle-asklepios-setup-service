package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.ScreenAuthority;
import com.dazzle.asklepios.domain.enumeration.Operation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScreenAuthorityRepository extends JpaRepository<ScreenAuthority, Long> {

    // جلب الصلاحيات حسب اسم الشاشة والعملية
    List<ScreenAuthority> findByScreenAndOperation(String screen, Operation operation);

    // جلب كل الصلاحيات لشاشة معينة
    List<ScreenAuthority> findByScreen(String screen);

    // جلب صلاحيات بناءً على مجموعة Authorities
    List<ScreenAuthority> findByAuthorityNameIn(List<String> authorityNames);
}
