package com.dazzle.asklepios.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// بننفذه مرة بعدين بنحذفه
@Component
@RequiredArgsConstructor
public class TranslationDataMigration {

    private final LanguageTranslationService languageTranslationService;

    @EventListener(ApplicationReadyEvent.class)
    public void migrate() {
        languageTranslationService.syncExistingEntityTranslations();
    }
}