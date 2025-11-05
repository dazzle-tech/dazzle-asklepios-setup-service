package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.CdtServiceMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface CdtServiceMappingRepository extends JpaRepository<CdtServiceMapping, Long> {

}
