package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.ScreenAuthority;
import com.dazzle.asklepios.domain.enumeration.Operation;
import com.dazzle.asklepios.repository.ScreenAuthorityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScreenAuthorityService {

    private final ScreenAuthorityRepository screenAuthorityRepository;

    // جلب كل الصلاحيات
    public List<ScreenAuthority> getAll() {
        return screenAuthorityRepository.findAll();
    }

    // جلب الصلاحيات حسب الشاشة
    public List<ScreenAuthority> getByScreen(String screen) {
        return screenAuthorityRepository.findByScreen(screen);
    }

    // جلب الصلاحيات لشاشة + عملية
    public List<ScreenAuthority> getByScreenAndOperation(String screen, Operation operation) {
        return screenAuthorityRepository.findByScreenAndOperation(screen, operation);
    }

    // جلب الصلاحيات بناءً على authorityName
    public List<ScreenAuthority> getByAuthorityNames(List<String> authorityNames) {
        return screenAuthorityRepository.findByAuthorityNameIn(authorityNames);
    }

    // إضافة صلاحية جديدة
    public ScreenAuthority save(ScreenAuthority screenAuthority) {
        return screenAuthorityRepository.save(screenAuthority);
    }

    // حذف صلاحية بالـ id
    public void delete(Long id) {
        screenAuthorityRepository.deleteById(id);
    }
}
