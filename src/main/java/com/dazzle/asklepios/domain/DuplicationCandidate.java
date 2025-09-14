package com.dazzle.asklepios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DuplicationCandidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // autoIncrement
    private Long id;

    private String role;

    private Boolean dob = false;
    private Boolean lastName = false;
    private Boolean documentNo = false;
    private Boolean mobileNumber = false;
    private Boolean gender = false;

    @Column(nullable = false)
    private String createdBy;

    private Instant createdDate;
    private String lastModifiedBy;
    private Instant lastModifiedDate;
}

