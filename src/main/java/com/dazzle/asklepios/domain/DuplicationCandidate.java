package com.dazzle.asklepios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)

@Table(name = "duplication_candidate")
public class DuplicationCandidate extends AbstractAuditingEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
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

    @NotNull
    @Column(nullable = false, length = 10)
    private Boolean isActive = true;

}



