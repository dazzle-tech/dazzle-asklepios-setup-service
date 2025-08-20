package com.dazzle.asklepios.domain;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ScreenAuthorityId implements Serializable {
    private Long screen;
    private String authorityName;
    private String operation;
}
