package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.FacilityType;
import com.dazzle.asklepios.service.dto.workingDay.WorkingDayJson;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Facility extends AbstractAuditingEntity<Long> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, length = 255)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 255)
    private FacilityType type;

    @NotNull
    @Column(nullable = false, length = 50)
    private String code;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @Column(name = "email_address", length = 100)
    private String emailAddress;

    @Column(length = 100)
    private String phone1;

    @Column(length = 100)
    private String phone2;

    @Column(length = 100)
    private String fax;

    @Column(name = "address_id", length = 100)
    private String addressId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "default_currency", nullable = false, length = 100)
    private Currency defaultCurrency;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "rule_id")
    private Long ruleId;

    @Column(name = "time_zone", length = 100)
    private String timeZone;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "working_days", columnDefinition = "json")
    private List<WorkingDayJson> workingDays;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_lab_department_id")
    private Department defaultLabDepartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_rad_department_id")
    private Department defaultRadDepartment;

    @Column(name="approving-diagnostic-test-settle-payment")
    private Boolean approvingDiagnosticTestSettlePayment = false;
}