package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.ScreenAuthority;
import com.dazzle.asklepios.domain.enumeration.Operation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScreenAuthorityRepository extends JpaRepository<ScreenAuthority, Long> {


    List<ScreenAuthority> findByScreenAndOperation(String screen, Operation operation);


    List<ScreenAuthority> findByScreen(String screen);

    List<ScreenAuthority> findByAuthorityNameIn(List<String> authorityNames);
}
