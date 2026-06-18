package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.WaseelItemMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WaseelItemMappingRepository extends JpaRepository<WaseelItemMapping, Long> {

}