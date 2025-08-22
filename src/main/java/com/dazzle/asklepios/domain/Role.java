package com.dazzle.asklepios.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Role {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id")
        private Long  id;

        @NotNull(message = "must not be null")
        @Column(name = "name",nullable = false)
        private String name;

        @Column(name = "type",nullable = false)
        private String type;

        @Column(name = "facility_id", insertable = false, updatable = false)
        private Long facilityId;

        @ManyToOne(fetch = FetchType.LAZY)
        @NotNull
        @JoinColumn(name = "facility_id", referencedColumnName = "id")
        private Facility facility;

}
