package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.enumeration.Module;
import com.dazzle.asklepios.domain.enumeration.Screen;
import com.dazzle.asklepios.service.dto.ModuleWithScreensDTO;
import com.dazzle.asklepios.service.dto.ScreenDTO;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class ModuleService {

    public List<ModuleWithScreensDTO> getModulesWithScreens() {

        return Arrays.stream(Module.values())
                .map(module -> {
                    List<ScreenDTO> screens = Arrays.stream(Screen.values())
                            .filter(screen -> screen.getModule() == module)
                            .map(screen -> new ScreenDTO(
                                    screen.getName(),
                                    screen.getDescription(),
                                    screen.getIcon(),
                                    screen.getViewOrder(),
                                    screen.getNavPath()
                            ))
                            .collect(Collectors.toList());

                    return new ModuleWithScreensDTO(
                            module.getName(),
                            module.getDescription(),
                            module.getIconImagePath(),
                            module.getViewOrder(),
                            screens
                    );
                })
                .collect(Collectors.toList());
    }
}
