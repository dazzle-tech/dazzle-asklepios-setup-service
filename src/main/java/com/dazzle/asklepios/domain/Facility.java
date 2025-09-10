package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.FacilityType;
import com.dazzle.asklepios.domain.enumeration.FacilityTypeConverter;
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
    @Column(nullable = false, length = 50)
    @Convert(converter = FacilityTypeConverter.class)
    private FacilityType type;

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

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Currency defaultCurrency;


}
