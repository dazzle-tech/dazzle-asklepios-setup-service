package com.dazzle.asklepios.domain;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "diagnostic_test_catalog_header")
public class DiagnosticTestCatalogHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long  id;

    @Column(name = "description")
    private String description;

    @Column(name = "type")
    private String type;

    @Column(name = "department_id")
    private String departmentId;

    @Column(name = "test_id")
    private String testId;

    @Column(name = "catalog_id")
    private String catalogId;

}
