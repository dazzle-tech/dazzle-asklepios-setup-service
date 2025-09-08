package com.dazzle.asklepios.domain;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.IdClass;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;


import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;


import java.io.Serializable;

@Entity
@IdClass(ScreenAuthorityId.class)
@Table(name = "screen_authority")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScreenAuthority implements Serializable {

    @Id
    @NotNull
    @ManyToOne
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    @Id
    @NotNull
    @Column(name = "authority_name", nullable = false)
    private String authorityName;

    @Id
    @NotNull
    private String operation;
}
