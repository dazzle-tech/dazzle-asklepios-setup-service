package com.dazzle.asklepios.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "uom_group")
public class UomGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 50)
    private String description;

    @Size(max = 50)
    private String name;

    @OneToMany(mappedBy = "group", orphanRemoval = true)
    @JsonManagedReference("group-units")
    private List<UomGroupUnit> units = new ArrayList<>();

    @OneToMany(mappedBy = "group", orphanRemoval = true)
    @JsonManagedReference("group-relations")
    private List<UomGroupsRelation> relations = new ArrayList<>();

}
