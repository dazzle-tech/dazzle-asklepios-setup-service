package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.MobileAppConfig;
import com.dazzle.asklepios.domain.enumeration.MobileAppConfigKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MobileAppConfigRepository extends JpaRepository<MobileAppConfig, Long> {

    List<MobileAppConfig> findAllByOrderByConfigKeyAsc();

    Optional<MobileAppConfig> findByConfigKey(MobileAppConfigKey configKey);

    boolean existsByConfigKey(MobileAppConfigKey configKey);
}
