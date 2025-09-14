package com.dazzle.asklepios.service.dto;

import java.util.List;

public class ModuleWithScreensDTO {
    private String name;
    private String description;
    private String icon;
    private int viewOrder;
    private List<ScreenDTO> screens;

    public ModuleWithScreensDTO(String name, String description, String icon, int viewOrder, List<ScreenDTO> screens) {
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.viewOrder = viewOrder;
        this.screens = screens;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getViewOrder() {
        return viewOrder;
    }

    public void setViewOrder(int viewOrder) {
        this.viewOrder = viewOrder;
    }

    public List<ScreenDTO> getScreens() {
        return screens;
    }

    public void setScreens(List<ScreenDTO> screens) {
        this.screens = screens;
    }
}
