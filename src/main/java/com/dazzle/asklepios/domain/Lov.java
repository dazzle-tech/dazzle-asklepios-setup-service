package com.dazzle.asklepios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ap_lov")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Lov implements Serializable {

    @Id
    @Column(name = "key", nullable = false)
    private String key;

    @Column(name = "lov_code")
    private String lovCode;

    @Column(name = "lov_name")
    private String lovName;

    @Column(name = "lov_description")
    private String lovDescription;

    @Column(name = "love_custom_code")
    private String loveCustomCode;

    @Column(name = "parent_lov")
    private String parentLov;

    @Column(name = "auto_select_default")
    private Boolean autoSelectDefault = false;

    @Column(name = "default_value_id")
    private String defaultValueId;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "deleted_by")
    private String deletedBy;

    @Column(name = "created_at", precision = 16)
    private BigDecimal createdAt;

    @Column(name = "updated_at", precision = 16)
    private BigDecimal updatedAt;

    @Column(name = "deleted_at", precision = 16)
    private BigDecimal deletedAt;

    @Column(name = "is_valid")
    private Boolean isValid = true;


}