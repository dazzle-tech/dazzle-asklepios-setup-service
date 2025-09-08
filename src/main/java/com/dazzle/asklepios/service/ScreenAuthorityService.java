package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.ScreenAuthority;
import com.dazzle.asklepios.domain.ScreenAuthorityId;
import com.dazzle.asklepios.repository.ScreenAuthorityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ScreenAuthorityService {

    private final ScreenAuthorityRepository repository;

    public ScreenAuthorityService(ScreenAuthorityRepository repository) {
        this.repository = repository;
    }

    public ScreenAuthority create(ScreenAuthority sa) {
        return repository.save(sa);
    }

}
