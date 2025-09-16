package com.dazzle.asklepios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

@Table(name = "duplication_candidate")
public class DuplicationCandidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String role;

    @Column(nullable = false)
    private Boolean dob = false;

    @Column(name = "last_name", nullable = false)
    private Boolean lastName = false;

    @Column(name = "document_no", nullable = false)
    private Boolean documentNo = false;

    @Column(name = "mobile_number", nullable = false)
    private Boolean mobileNumber = false;

    @Column(nullable = false)
    private Boolean gender = false;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_date")
    private Instant createdDate;

    @Column(name = "last_modified_by")
    private String lastModifiedBy;

    @Column(name = "last_modified_date")
    private Instant lastModifiedDate;

    @Column(name = "active", nullable = false)
    private Boolean active = true;


}
