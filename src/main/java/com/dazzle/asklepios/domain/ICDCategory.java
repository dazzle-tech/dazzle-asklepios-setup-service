package com.dazzle.asklepios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Immutable
@Table(name = "icd_category")
public class ICDCategory implements Serializable {

    @Id
    @Column(name = "category_code", nullable = false, length = 50)
    private String categoryCode;

    @NotNull
    @Column(name = "icd_coding", nullable = false, length = 10)
    private String icdCoding;

    @NotNull
    @Column(name = "category_name", nullable = false, length = 500)
    private String categoryName;

    @Column(name = "category_description", columnDefinition = "text")
    private String categoryDescription;

    @Column(name = "parent_category_code", length = 50)
    private String parentCategoryCode;
}
