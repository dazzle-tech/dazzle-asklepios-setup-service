package com.dazzle.asklepios.service.dto.room;

import com.dazzle.asklepios.domain.enumeration.DepartmentType;
import com.dazzle.asklepios.domain.enumeration.Gender;
import com.dazzle.asklepios.domain.enumeration.RoomType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RoomUpdateDTO(

        @NotNull
        Long id,

        @NotNull
        Long facilityId,

        @NotNull
        DepartmentType departmentType,

        @NotNull
        Long departmentId,

        @NotBlank
        String name,

        @NotNull
        RoomType type,

        String floor,

        @NotNull
        Boolean isSpecificGender,

        Gender gender,

        @NotNull
        Boolean appointable,

        @NotNull
        Integer parallelCapacityValue,

        Integer defaultDurationMinutes,

        Integer defaultBufferBeforeMinutes,

        Integer defaultBufferAfterMinutes

) implements Serializable {
}