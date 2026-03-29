package com.dazzle.asklepios.service.dto.bedRoomService;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BedRoomServiceCreateDTO(

        @NotNull
        Long roomId,

        @NotNull
        Long serviceId,

        @NotNull
        Boolean bedSpecific,

        Long bedId,

        String rule

) implements Serializable {
}