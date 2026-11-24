package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.biling.PayorCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@EqualsAndHashCode(callSuper = false)
@Table(name = "payor")
public class Payor extends AbstractAuditingEntity<Long> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Payor code cannot be null")
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @NotNull(message = "Payor name cannot be null")
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @NotNull(message = "Category cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private PayorCategory category;

    // Contact
    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "contract_manager_contact", length = 255)
    private String contractManagerContact;

    @Column(name = "nphies_id", length = 100, unique = true)
    private String nphiesId;

    @Column(name = "waseel_payer_id", length = 100)
    private String waseelPayerId;

    @Column(name = "tpa_nphies_id", length = 100)
    private String tpaNphiesId;

    @NotNull
    @Column(name = "is_waseel_enabled", nullable = false)
    private Boolean isWaseelEnabled = true;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
