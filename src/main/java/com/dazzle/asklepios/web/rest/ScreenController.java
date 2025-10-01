package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.enumeration.Screen;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/setup/screen")
public class ScreenController {



    public ScreenController() {

    }

    @GetMapping
    public ResponseEntity<List<Screen>> getAllScreens() {
        List<Screen> screens = Arrays.asList(Screen.values());
        return ResponseEntity.ok(screens); // status 200 + body
    }

}
