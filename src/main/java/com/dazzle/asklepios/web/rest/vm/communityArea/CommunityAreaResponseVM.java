package com.dazzle.asklepios.web.rest.vm.communityArea;

import com.dazzle.asklepios.domain.CommunityArea;

import java.io.Serializable;

public record CommunityAreaResponseVM(
        Long id,
        Long communityId,
        String name,
        Boolean isActive
) implements Serializable {

    public static CommunityAreaResponseVM ofEntity(CommunityArea entity) {
        return new CommunityAreaResponseVM(
                entity.getId(),
                entity.getCommunity() != null ? entity.getCommunity().getId() : null,
                entity.getName(),
                entity.getIsActive()
        );
    }
}
