package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.Language;
import com.dazzle.asklepios.domain.LanguageTranslation;
import com.dazzle.asklepios.repository.LanguageRepository;
import com.dazzle.asklepios.repository.LanguageTranslationRepository;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.dazzle.asklepios.annotation.Translatable;
import jakarta.persistence.Id;

import java.lang.reflect.Field;
import org.springframework.context.i18n.LocaleContextHolder;

@Service
@Transactional
public class LanguageTranslationService {

    private static final Logger LOG = LoggerFactory.getLogger(LanguageTranslationService.class);

    private final LanguageTranslationRepository translationRepository;
    private final LanguageRepository languageRepository;
    private final TranslatableEntityScannerService translatableEntityScannerService;
    private final EntityManager entityManager;
    private final EnumRegistry enumRegistry;


    public LanguageTranslationService(LanguageTranslationRepository translationRepository, LanguageRepository languageRepository, TranslatableEntityScannerService translatableEntityScannerService, EntityManager entityManager, EnumRegistry enumRegistry) {
        this.translationRepository = translationRepository;
        this.languageRepository = languageRepository;
        this.translatableEntityScannerService = translatableEntityScannerService;
        this.entityManager = entityManager;
        this.enumRegistry = enumRegistry;
    }


    public LanguageTranslation create(LanguageTranslation vm) {
        LOG.debug("Request to create LanguageTranslation : {}", vm);

        translationRepository.findByLangKeyAndTranslationKey(vm.getLangKey(), vm.getTranslationKey())
                .ifPresent(t -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Translation already exists for (" + vm.getLangKey() + ", " + vm.getTranslationKey() + ")"
                    );
                });

        LanguageTranslation entity = new LanguageTranslation();
        entity.setLangKey(vm.getLangKey());
        entity.setTranslationKey(vm.getTranslationKey());
        entity.setTranslationText(vm.getTranslationText());
        entity.setVerified(Boolean.TRUE.equals(vm.getVerified()));
        entity.setTranslated(Boolean.TRUE.equals(vm.getTranslated()));

        try {
            return translationRepository.save(entity);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid translation data", ex);
        }
    }

    public Optional<LanguageTranslation> update(Long id, LanguageTranslation vm) {
        LOG.debug("Request to update LanguageTranslation id={} with data: {}", id, vm);

        return translationRepository.findById(id).map(existing -> {
            // langKey & translationKey are identity — don’t change here
            existing.setTranslationText(vm.getTranslationText());
            if (vm.getVerified() != null) existing.setVerified(vm.getVerified());
            if (vm.getTranslated() != null) existing.setTranslated(vm.getTranslated());
            LanguageTranslation updated = translationRepository.save(existing);
            LOG.debug("LanguageTranslation id={} updated successfully", id);
            return updated;
        });
    }

    @Transactional(readOnly = true)
    public List<LanguageTranslation> findAll() {
        LOG.debug("Request to get all LanguageTranslations");
        return translationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<LanguageTranslation> findOne(Long id) {
        LOG.debug("Request to get LanguageTranslation : {}", id);
        return translationRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<LanguageTranslation> findByPair(String langKey, String translationKey) {
        LOG.debug("Request to get LanguageTranslation by pair ({}, {})", langKey, translationKey);
        return translationRepository.findByLangKeyAndTranslationKey(langKey, translationKey);
    }

    @Transactional(readOnly = true)
    public List<LanguageTranslation> findByLangKey(String langKey) {
        return translationRepository.findAllByLangKey(langKey);
    }

    @Transactional(readOnly = true)
    public Page<LanguageTranslation> findByLangKeyAndTranslationText(
            String langKey, String value, Pageable pageable
    ) {
        LOG.debug("Request to get LanguageTranslations by (langKey={}, value contains='{}') pageable={}", langKey, value, pageable);
        return translationRepository.findByLangKeyAndTranslationTextContainingIgnoreCase(langKey, value, pageable);
    }


    public boolean delete(Long id) {
        LOG.debug("Request to delete LanguageTranslation : {}", id);
        if (!translationRepository.existsById(id)) {
            return false;
        }
        translationRepository.deleteById(id);
        return true;
    }

    private String getResourceType(Class<?> entityClass) {
        return entityClass.getSimpleName().toUpperCase();
    }

    public List<Field> getTranslatableFields(Class<?> entityClass) {

        List<Field> fields = new ArrayList<>();

        Class<?> currentClass = entityClass;

        while (currentClass != null && currentClass != Object.class) {

            for (Field field : currentClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Translatable.class)) {
                    fields.add(field);
                }
            }

            currentClass = currentClass.getSuperclass();
        }

        return fields;
    }

    public void createMissingTranslations(Object entity) {

        if (entity == null) {
            return;
        }

        String currentLanguage =
                LocaleContextHolder.getLocale().getLanguage();

        LOG.error("Current locale = {}", LocaleContextHolder.getLocale());
        LOG.error("Current language = {}", currentLanguage);

        List<String> languageKeys = languageRepository.findAll()
                .stream()
                .map(Language::getLangKey)
                .toList();
        LOG.error("Current language = {}", currentLanguage);
        LOG.error("Available language keys = {}", languageKeys);
        if (languageKeys.isEmpty()) {
            return;
        }

        String resourceType = entity.getClass()
                .getSimpleName()
                .toUpperCase();

        String resourceKey = getEntityId(entity);

        if (resourceKey == null) {
            throw new IllegalArgumentException(
                    "Cannot create translations for entity without ID"
            );
        }

        List<Field> translatableFields =
                getTranslatableFields(entity.getClass());

        for (String langKey : languageKeys) {

            for (Field field : translatableFields) {

                boolean exists =
                        translationRepository
                                .findByLangKeyAndResourceTypeAndResourceKeyAndFieldName(
                                        langKey,
                                        resourceType,
                                        resourceKey,
                                        field.getName()
                                )
                                .isPresent();

                if (exists) {
                    continue;
                }

                boolean isCurrentLanguage =
                        langKey.equalsIgnoreCase(currentLanguage);
                LOG.error(
                        "langKey = [{}], currentLanguage = [{}], isCurrentLanguage = [{}]",
                        langKey,
                        currentLanguage,
                        isCurrentLanguage
                );


                String translationText = null;

                if (isCurrentLanguage) {
                    translationText =
                            getTranslatableFieldValue(entity, field);
                }

                LanguageTranslation translation =
                        LanguageTranslation.builder()
                                .langKey(langKey)
                                .resourceType(resourceType)
                                .resourceKey(resourceKey)
                                .fieldName(field.getName())
                                .translationText(translationText)
                                .verified(isCurrentLanguage)
                                .translated(isCurrentLanguage)
                                .build();

                translationRepository.save(translation);
            }
        }
    }

    private String getTranslatableFieldValue(Object entity, Field field) {
        try {
            field.setAccessible(true);

            Object value = field.get(entity);

            return value != null ? value.toString() : null;

        } catch (IllegalAccessException e) {
            throw new IllegalStateException(
                    "Unable to read translatable field: " + field.getName(),
                    e
            );
        }
    }

    private String getEntityId(Object entity) {

        Class<?> currentClass = entity.getClass();

        while (currentClass != null && currentClass != Object.class) {

            for (Field field : currentClass.getDeclaredFields()) {

                if (field.isAnnotationPresent(Id.class)) {

                    try {
                        field.setAccessible(true);

                        Object id = field.get(entity);

                        return id != null ? id.toString() : null;

                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException(
                                "Unable to read entity ID",
                                e
                        );
                    }
                }
            }

            currentClass = currentClass.getSuperclass();
        }

        throw new IllegalArgumentException(
                "Entity does not have an @Id field: "
                        + entity.getClass().getName()
        );
    }

    public void syncForNewLanguage(String langKey) {

        List<Class<?>> entityClasses =
                translatableEntityScannerService.findTranslatableEntities();

        for (Class<?> entityClass : entityClasses) {

            syncEntityTranslationsForLanguage(
                    entityClass,
                    langKey
            );
        }

        Map<String, List<String>> allEnums =
                getAllEnums();

        syncEnumTranslationsForLanguage(
                langKey,
                allEnums
        );
    }

    public void syncEntityTranslationsForLanguage(
            Class<?> entityClass,
            String langKey
    ) {
        List<Field> translatableFields =
                getTranslatableFields(entityClass);

        if (translatableFields.isEmpty()) {
            return;
        }

        List<?> entities = entityManager
                .createQuery(
                        "SELECT e FROM " + entityClass.getSimpleName() + " e",
                        entityClass
                )
                .getResultList();

        String resourceType = entityClass.getSimpleName().toUpperCase();

        for (Object entity : entities) {

            String resourceKey = getEntityId(entity);

            if (resourceKey == null) {
                continue;
            }

            for (Field field : translatableFields) {

                boolean exists =
                        translationRepository
                                .findByLangKeyAndResourceTypeAndResourceKeyAndFieldName(
                                        langKey,
                                        resourceType,
                                        resourceKey,
                                        field.getName()
                                )
                                .isPresent();

                if (exists) {
                    continue;
                }

                LanguageTranslation translation =
                        LanguageTranslation.builder()
                                .langKey(langKey)
                                .resourceType(resourceType)
                                .resourceKey(resourceKey)
                                .fieldName(field.getName())
                                .translationText(null)
                                .verified(false)
                                .translated(false)
                                .build();

                translationRepository.save(translation);
            }
        }
    }

    public void syncExistingEntityTranslations() {

        List<Class<?>> entityClasses =
                translatableEntityScannerService.findTranslatableEntities();

        List<Language> languages = languageRepository.findAll();

        for (Language language : languages) {

            for (Class<?> entityClass : entityClasses) {

                syncExistingEntityTranslationsForLanguage(
                        entityClass,
                        language.getLangKey()
                );
            }
        }
    }

    private void syncExistingEntityTranslationsForLanguage(
            Class<?> entityClass,
            String langKey
    ) {

        List<Field> translatableFields =
                getTranslatableFields(entityClass);

        if (translatableFields.isEmpty()) {
            return;
        }

        List<?> entities = entityManager
                .createQuery(
                        "SELECT e FROM " + entityClass.getSimpleName() + " e",
                        entityClass
                )
                .getResultList();

        String resourceType =
                entityClass.getSimpleName().toUpperCase();

        String sourceLanguage = "en";

        for (Object entity : entities) {

            String resourceKey = getEntityId(entity);

            if (resourceKey == null) {
                continue;
            }

            for (Field field : translatableFields) {

                boolean exists =
                        translationRepository
                                .findByLangKeyAndResourceTypeAndResourceKeyAndFieldName(
                                        langKey,
                                        resourceType,
                                        resourceKey,
                                        field.getName()
                                )
                                .isPresent();

                if (exists) {
                    continue;
                }

                boolean isSourceLanguage =
                        langKey.equalsIgnoreCase(sourceLanguage);

                String translationText = null;

                if (isSourceLanguage) {
                    translationText =
                            getTranslatableFieldValue(entity, field);
                }

                LanguageTranslation translation =
                        LanguageTranslation.builder()
                                .langKey(langKey)
                                .resourceType(resourceType)
                                .resourceKey(resourceKey)
                                .fieldName(field.getName())
                                .translationText(translationText)
                                .verified(isSourceLanguage)
                                .translated(isSourceLanguage)
                                .build();

                translationRepository.save(translation);
            }
        }
    }

    private void syncEnumTranslationsForLanguage(
            String langKey,
            Map<String, List<String>> allEnums
    ) {

        for (Map.Entry<String, List<String>> entry : allEnums.entrySet()) {

            String enumType = entry.getKey();

            for (String value : entry.getValue()) {

                boolean exists =
                        translationRepository
                                .findByLangKeyAndResourceTypeAndResourceKeyAndEnumType(
                                        langKey,
                                        "ENUM",
                                        value,
                                        enumType
                                )
                                .isPresent();

                if (exists) {
                    continue;
                }

                boolean isEnglish =
                        "en".equalsIgnoreCase(langKey);

                LanguageTranslation translation =
                        LanguageTranslation.builder()
                                .langKey(langKey)
                                .resourceType("ENUM")
                                .resourceKey(value)
                                .fieldName(null)
                                .enumType(enumType)
                                .translationText(
                                        isEnglish
                                                ? formatEnumLabel(value)
                                                : null
                                )
                                .verified(isEnglish)
                                .translated(isEnglish)
                                .build();

                translationRepository.save(translation);
            }
        }
    }

    private String formatEnumLabel(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        String formatted = value
                .replace("_", " ")
                .toLowerCase();

        return java.util.Arrays.stream(formatted.split(" "))
                .map(word -> word.isEmpty()
                        ? word
                        : Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(java.util.stream.Collectors.joining(" "));
    }

    // بنحذفه بعدين
    public void syncEnumTranslationsForExistingLanguages() {

        LOG.info("BEFORE getAllEnums");

        Map<String, List<String>> allEnums =
                getAllEnums();

        LOG.info("AFTER getAllEnums");
        LOG.info("All enums received: {}", allEnums);

        List<String> languageKeys = languageRepository.findAll()
                .stream()
                .map(Language::getLangKey)
                .toList();

        for (String langKey : languageKeys) {
            syncEnumTranslationsForLanguage(langKey, allEnums);
        }
    }

    private Map<String, List<String>> getAllEnums() {
        return new HashMap<>(enumRegistry.getAll());
    }



    @Transactional(readOnly = true)
    public Optional<String> findTranslatedField(
            String langKey,
            String resourceType,
            String resourceKey,
            String fieldName
    ) {
        return translationRepository
                .findByLangKeyAndResourceTypeAndResourceKeyAndFieldName(
                        langKey,
                        resourceType,
                        resourceKey,
                        fieldName
                )
                .map(LanguageTranslation::getTranslationText);
    }

    @Transactional(readOnly = true)
    public Map<String, String> findTranslatedFields(
            Object entity,
            String langKey
    ) {
        if (entity == null || langKey == null || langKey.isBlank()) {
            return Map.of();
        }

        String resourceType = entity.getClass()
                .getSimpleName()
                .toUpperCase();

        String resourceKey = getEntityId(entity);

        if (resourceKey == null) {
            return Map.of();
        }

        List<Field> translatableFields =
                getTranslatableFields(entity.getClass());

        if (translatableFields.isEmpty()) {
            return Map.of();
        }

        Map<String, String> translations = new HashMap<>();

        for (Field field : translatableFields) {

            translationRepository
                    .findByLangKeyAndResourceTypeAndResourceKeyAndFieldName(
                            langKey,
                            resourceType,
                            resourceKey,
                            field.getName()
                    )
                    .map(LanguageTranslation::getTranslationText)
                    .filter(text -> text != null && !text.isBlank())
                    .ifPresent(text ->
                            translations.put(field.getName(), text)
                    );
        }

        return translations;
    }
    @Transactional(readOnly = true)
    public Map<String, String> getTranslatedFields(
            String langKey,
            Object entity
    ) {
        Map<String, String> translations = new HashMap<>();

        if (entity == null) {
            return translations;
        }

        String resourceType = entity.getClass()
                .getSimpleName()
                .toUpperCase();

        String resourceKey = getEntityId(entity);

        if (resourceKey == null) {
            return translations;
        }

        List<Field> translatableFields =
                getTranslatableFields(entity.getClass());

        for (Field field : translatableFields) {

            translationRepository
                    .findByLangKeyAndResourceTypeAndResourceKeyAndFieldName(
                            langKey,
                            resourceType,
                            resourceKey,
                            field.getName()
                    )
                    .map(LanguageTranslation::getTranslationText)
                    .filter(text -> !text.isBlank())
                    .ifPresent(text ->
                            translations.put(field.getName(), text)
                    );
        }

        return translations;
    }
    public <T extends Record> T applyTranslations(
            T response,
            Map<String, String> translations
    ) {
        if (response == null || translations == null || translations.isEmpty()) {
            return response;
        }

        try {
            Class<?> responseClass = response.getClass();

            RecordComponent[] components =
                    responseClass.getRecordComponents();

            Object[] values = new Object[components.length];

            Class<?>[] parameterTypes = new Class<?>[components.length];

            for (int i = 0; i < components.length; i++) {

                RecordComponent component = components[i];

                parameterTypes[i] = component.getType();

                Object value =
                        component.getAccessor().invoke(response);

                /*
                 * إذا اسم الـ field موجود ضمن translations
                 * وكان field في الـ Response من نوع String،
                 * نستبدل القيمة بالترجمة.
                 */
                if (component.getType().equals(String.class)
                        && translations.containsKey(component.getName())) {

                    String translatedValue =
                            translations.get(component.getName());

                    if (translatedValue != null
                            && !translatedValue.isBlank()) {

                        value = translatedValue;
                    }
                }

                values[i] = value;
            }

            Constructor<?> constructor =
                    responseClass.getDeclaredConstructor(parameterTypes);

            constructor.setAccessible(true);

            @SuppressWarnings("unchecked")
            T translatedResponse =
                    (T) constructor.newInstance(values);

            return translatedResponse;

        } catch (ReflectiveOperationException e) {

            throw new IllegalStateException(
                    "Unable to apply translations to response: "
                            + response.getClass().getName(),
                    e
            );
        }
    }
}
