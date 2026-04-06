package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.TimeZone;
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
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "default_time_zone", length = 100, nullable = false)
    @NotNull
    @Enumerated(EnumType.STRING)
    private TimeZone defaultTimeZone;

    @ManyToOne
    @JoinColumn(name = "default_language_id", nullable = false)
    private Language defaultLanguage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "working_days", columnDefinition = "json", nullable = false)
    @Builder.Default
    private List<WorkingDayJson> workingDays = new ArrayList<>();
}
