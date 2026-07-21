package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.BillingConfigurationKey;
import com.dazzle.asklepios.domain.enumeration.BillingConfigurationStatus;
import com.dazzle.asklepios.domain.enumeration.BillingConfigurationValueType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(
        name = "billing_configuration"
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingConfiguration  extends AbstractAuditingEntity<Long> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @Column(name = "facility_id", nullable = false)
    private Long facilityId ;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(
            name = "configuration_key",
            nullable = false,
            length = 150
    )
    private BillingConfigurationKey configurationKey;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(
            name = "value_type",
            nullable = false,
            length = 30
    )
    private BillingConfigurationValueType valueType;

    @Lob
    @Column(name = "configuration_value")
    private String configurationValue;

    @Size(max = 255)
    @Column(name = "enum_code", length = 255)
    private String enumCode;

    @Size(max = 500)
    @Column(name = "description")
    private String description;

    @NotNull
    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private BillingConfigurationStatus status =
            BillingConfigurationStatus.DRAFT;


}