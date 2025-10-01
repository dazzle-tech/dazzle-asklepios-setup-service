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


    public List<ScreenAuthority> getAll() {
        return screenAuthorityRepository.findAll();
    }

}
