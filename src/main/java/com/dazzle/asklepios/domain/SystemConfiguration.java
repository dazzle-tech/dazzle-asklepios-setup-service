package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.SystemConfigKey;
import com.dazzle.asklepios.domain.enumeration.SystemConfigType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "system_configuration")
@Getter
@Setter
@NoArgsConstructor
public class SystemConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "config_key", nullable = false, unique = true)
    private SystemConfigKey configKey;

    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "config_type", nullable = false)
    private SystemConfigType configType;

    @Column(name = "description")
    private String description;
}