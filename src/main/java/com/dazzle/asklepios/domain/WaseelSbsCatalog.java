package com.dazzle.asklepios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "waseel_sbs_catalog")
public class WaseelSbsCatalog extends AbstractAuditingEntity<Long> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "waseel_item_type", nullable = false, length = 100)
    private String waseelItemType;

    @Column(name = "sbs_code", nullable = false, unique = true, length = 100)
    private String sbsCode;

    @Column(name = "update_type", length = 20)
    private String updateType;

    @Column(name = "revision_details")
    private String revisionDetails;

    @Column(name = "short_description")
    private String shortDescription;

    @Column(name = "long_description")
    private String longDescription;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "source_file_name")
    private String sourceFileName;

}