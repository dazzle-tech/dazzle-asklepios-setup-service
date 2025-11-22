package com.dazzle.asklepios.web.rest.vm.districtCommunity;

import com.dazzle.asklepios.domain.DistrictCommunity;
import jakarta.validation.constraints.NotEmpty;
import java.io.Serializable;

public record DistrictCommunityCreateVM(
        @NotEmpty String name,
        Boolean isActive
) implements Serializable {

    public static DistrictCommunityCreateVM ofEntity(DistrictCommunity entity) {
        return new DistrictCommunityCreateVM(
                entity.getName(),
                entity.getIsActive()
        );
    }
}
