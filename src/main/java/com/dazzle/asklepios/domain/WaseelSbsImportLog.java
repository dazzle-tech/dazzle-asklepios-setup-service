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
@Table(name = "waseel_sbs_import_log")
public class WaseelSbsImportLog extends AbstractAuditingEntity<Long> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name",columnDefinition = "TEXT")
    private String fileName;

    @Column(name = "total_rows")
    private Long totalRows;

    @Column(name = "success_rows")
    private Long successRows;

    @Column(name = "failed_rows" )
    private Long failedRows;

    @Column(name = "error_details",columnDefinition = "TEXT")
    private String errorDetails;

}