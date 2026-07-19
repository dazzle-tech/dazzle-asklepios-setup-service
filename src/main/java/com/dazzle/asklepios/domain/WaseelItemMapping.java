package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "waseel_item_mapping")
public class WaseelItemMapping extends AbstractAuditingEntity<Long> implements Serializable {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @NotNull
        @Enumerated
        @Column(name = "item_type", length = 50)
        private BillingItemTypes itemType;

        @Column(name = "source_id")
        private Long sourceId;

        @Column(name = "item_code", length = 255)
        private String itemCode;

        @Column(name = "item_name", length = 1000)
        private String itemName;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "sbs_catalog_id", nullable = false)
        private WaseelSbsCatalog sbsCatalog;

        @Column(name = "requires_preauth", nullable = false)
        private Boolean requiresPreauth = false;

        @Column(name = "is_active", nullable = false)
        private Boolean isActive = true;

        @Column(name = "notes", length = 1000)
        private String notes;
    }