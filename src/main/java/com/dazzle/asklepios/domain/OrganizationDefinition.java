package com.dazzle.asklepios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OrganizationDefinition extends AbstractAuditingEntity<Long> implements Serializable {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(length = 500)
    private String address;

    @Column(length = 255)
    private String contactName;

    @Column(length = 500)
    private String contactAddress;

    @Column(length = 255)
    private String contactEmail;

    @Column(length = 50)
    private String contactMobile;

    @Column(length = 50)
    private String contactLandNumber;

    @NotNull
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal taxValue;
}
