package com.dazzle.asklepios.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "cdt_dental_action")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CdtDentalAction implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dental_action_id")
    private DentalAction dentalAction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cdt_id", referencedColumnName = "id")
    private CdtCode cdtId;
}
