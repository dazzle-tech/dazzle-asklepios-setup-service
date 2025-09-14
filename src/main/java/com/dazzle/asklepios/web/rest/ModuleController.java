package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.dto.ModuleWithScreensDTO;
import com.dazzle.asklepios.service.ModuleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/setup/api/modules")
public class ModuleController {

    private final ModuleService moduleService;

    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    @GetMapping("")
    public List<ModuleWithScreensDTO> getModules() {
        return moduleService.getModulesWithScreens();
    }
}
