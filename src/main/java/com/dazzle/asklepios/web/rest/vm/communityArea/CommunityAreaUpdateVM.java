package com.dazzle.asklepios.web.rest.vm.communityArea;

import com.dazzle.asklepios.domain.CommunityArea;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record CommunityAreaUpdateVM(
        @NotNull Long id,
        @NotNull Long communityId,
        @NotEmpty String name,
        Boolean isActive
) implements Serializable {

    public static CommunityAreaUpdateVM ofEntity(CommunityArea entity) {
        return new CommunityAreaUpdateVM(
                entity.getId(),
                entity.getCommunity() != null ? entity.getCommunity().getId() : null,
                entity.getName(),
                entity.getIsActive()
        );
    }
}
