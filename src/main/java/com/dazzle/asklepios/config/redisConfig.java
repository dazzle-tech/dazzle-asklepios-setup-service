package com.dazzle.asklepios.config;

import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.repository.UomGroupRelationRepository;
import com.dazzle.asklepios.repository.UomGroupRepository;
import com.dazzle.asklepios.repository.UomGroupUnitRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.Set;


@Configuration
public class redisConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues();

        Set<String> cacheNames = Set.of(
                FacilityRepository.FACILITIES,
                UomGroupRepository.UOMGROUP,
                UomGroupUnitRepository.UOMGROUPUNIT,
                UomGroupRelationRepository.UOMGROUPRELATION
        );

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(config)
                .initialCacheNames(cacheNames)
                .build();
    }
}

