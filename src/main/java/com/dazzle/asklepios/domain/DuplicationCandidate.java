package com.dazzle.asklepios.domain;

import jakarta.persistence.*;
import lombok.*;

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

