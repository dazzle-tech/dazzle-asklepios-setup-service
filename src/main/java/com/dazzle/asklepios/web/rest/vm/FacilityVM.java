package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.enumeration.FacilityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * View Model for creating/updating a Facility via REST.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class FacilityVM {

    @NotBlank
    private String name;

    @NotNull
    private FacilityType type;
}
