package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.enumeration.Screen;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ScreenService {


    public List<Screen> getAllScreens() {
        return Arrays.asList(Screen.values());
    }
}
