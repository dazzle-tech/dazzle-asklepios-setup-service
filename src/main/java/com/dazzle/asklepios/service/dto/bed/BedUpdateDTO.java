package com.dazzle.asklepios.service.dto.bed;

import com.dazzle.asklepios.domain.enumeration.BedType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BedUpdateDTO(

        @NotNull
        Long id,

        @NotNull
        Long roomId,

        @NotBlank
        String name,

        String locationDetails,

        @NotNull
        BedType type

) implements Serializable {
}