package com.dazzle.asklepios.web.rest.vm.districtCommunity;

import com.dazzle.asklepios.domain.DistrictCommunity;
import java.io.Serializable;

public record DistrictCommunityResponseVM(
        Long id,
        Long districtId,
        String name,
        Boolean isActive
) implements Serializable {

    public static DistrictCommunityResponseVM ofEntity(DistrictCommunity entity) {
        return new DistrictCommunityResponseVM(
                entity.getId(),
                entity.getDistrict() != null ? entity.getDistrict().getId() : null,
                entity.getName(),
                entity.getIsActive()
        );
    }
}
