package com.dazzle.asklepios.web.rest.vm.communityArea;

import com.dazzle.asklepios.domain.CommunityArea;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CommunityAreaCreateVM(
        @NotEmpty String name,
        Boolean isActive
) implements Serializable {

    public static CommunityAreaCreateVM ofEntity(CommunityArea area) {
        return new CommunityAreaCreateVM(area.getName(), area.getIsActive());
    }
}
