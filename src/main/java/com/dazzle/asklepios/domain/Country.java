    package com.dazzle.asklepios.domain;

    import com.dazzle.asklepios.domain.enumeration.CountryName;
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

    import java.io.Serial;
    import java.io.Serializable;

    @Entity
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @EqualsAndHashCode(callSuper = false)
    @Table(name = "country")
    public class Country extends AbstractAuditingEntity<Long> implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @NotNull
        @Enumerated(EnumType.STRING)
        @Column(name = "name", nullable = false, length = 150)
        private CountryName name;

        @NotNull
        @Column(name = "code", nullable = false, length = 50)
        private String code;

        @NotNull
        @Column(name = "is_active", nullable = false)
        private Boolean isActive;
    }
