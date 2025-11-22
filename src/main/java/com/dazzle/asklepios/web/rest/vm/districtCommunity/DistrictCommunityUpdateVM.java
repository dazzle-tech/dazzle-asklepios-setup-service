package com.dazzle.asklepios.web.rest.vm.districtCommunity;

import com.dazzle.asklepios.domain.DistrictCommunity;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

public record DistrictCommunityUpdateVM(
        @NotNull Long id,
        @NotNull Long districtId,
        @NotEmpty String name,
        Boolean isActive
) implements Serializable {

    public static DistrictCommunityUpdateVM ofEntity(DistrictCommunity entity) {
        return new DistrictCommunityUpdateVM(
                entity.getId(),
                entity.getDistrict() != null ? entity.getDistrict().getId() : null,
                entity.getName(),
                entity.getIsActive()
        );
    }
}
