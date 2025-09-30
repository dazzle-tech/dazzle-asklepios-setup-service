package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.EnumRegistry;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/setup/enums")
public class EnumApiResourceController {

    private final EnumRegistry enumRegistry;

    public EnumApiResourceController(EnumRegistry enumRegistry) {
        this.enumRegistry = enumRegistry;
    }

    @GetMapping
    public Map<String, List<String>> getAllEnums() {
        return enumRegistry.getAll();
    }
}
