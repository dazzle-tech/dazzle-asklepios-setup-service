package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.FacilityType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Facility implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, length = 255)
    private String name;

    @NotNull
    @Column(nullable = false, length = 255)
    private String type;

    private LocalDate registrationDate;

    @Column(length = 100)
    private String emailAddress;

    @Column(length = 100)
    private String phone1;

    @Column(length = 100)
    private String phone2;

    @Column(length = 100)
    private String fax;

    @Column(length = 100)
    private String addressId;

    @Column(length = 100)
    private String defaultCurrencyLkey;

    @Column(nullable = false)
    private Boolean isValid = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isValid == null) {
            isValid = true;
        }
    }

}
