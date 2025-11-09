package com.dazzle.asklepios.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
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
@Table(
        name = "uom_group",
        uniqueConstraints = @UniqueConstraint(name = "uq_code", columnNames = "code")
)
public class UomGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 50)
    private String description;

    @Size(max = 50)
    private String name;

    @Size(max = 50)
    @Column(nullable = false)
    private String code;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("group-units")
    private List<UomGroupUnit> units = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("group-relations")
    private List<UomGroupsRelation> relations = new ArrayList<>();

}
