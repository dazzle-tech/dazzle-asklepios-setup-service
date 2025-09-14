package com.dazzle.asklepios.service.dto;
import lombok.Data;

@Data
public class ScreenDTO {
    private String name;
    private String description;
    private String icon;
    private int viewOrder;
    private String navPath;

    public ScreenDTO(String name, String description, String icon, int viewOrder, String navPath) {
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.viewOrder = viewOrder;
        this.navPath = navPath;
    }


}
