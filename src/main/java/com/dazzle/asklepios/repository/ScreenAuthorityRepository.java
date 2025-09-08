package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.ScreenAuthority;
import com.dazzle.asklepios.domain.ScreenAuthorityId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScreenAuthorityRepository extends JpaRepository<ScreenAuthority, ScreenAuthorityId> {

}

