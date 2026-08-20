package com.dazzle.asklepios.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnumTranslationInitializer implements CommandLineRunner {

    private final LanguageTranslationService languageTranslationService;

    @Override
    public void run(String... args) {
        languageTranslationService.syncEnumTranslationsForExistingLanguages();
    }
}