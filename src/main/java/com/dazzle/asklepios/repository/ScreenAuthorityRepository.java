package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.ScreenAuthority;
import com.dazzle.asklepios.domain.enumeration.Operation;
import com.dazzle.asklepios.domain.enumeration.Screen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScreenAuthorityRepository extends JpaRepository<ScreenAuthority, Long> {


    List<ScreenAuthority> findByScreenAndOperation(Screen screen, Operation operation);

    List<ScreenAuthority> findByScreen(Screen screen);

}
