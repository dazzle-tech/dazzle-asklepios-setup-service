package com.dazzle.asklepios.service;

import com.dazzle.asklepios.annotation.Translatable;
import jakarta.persistence.Entity;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

// بننفذه مرة بعدين بنحذفه
@Component
public class TranslatableEntityScannerService {

    private static final String DOMAIN_PACKAGE =
            "com.dazzle.asklepios.domain";

    public List<Class<?>> findTranslatableEntities() {

        List<Class<?>> entities = new ArrayList<>();

        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);

        scanner.addIncludeFilter(
                new AnnotationTypeFilter(Entity.class)
        );

        for (var beanDefinition :
                scanner.findCandidateComponents(DOMAIN_PACKAGE)) {

            try {
                Class<?> entityClass =
                        Class.forName(beanDefinition.getBeanClassName());

                if (hasTranslatableField(entityClass)) {
                    entities.add(entityClass);
                }

            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(
                        "Could not load entity class: "
                                + beanDefinition.getBeanClassName(),
                        e
                );
            }
        }

        return entities;
    }

    private boolean hasTranslatableField(Class<?> entityClass) {

        Class<?> currentClass = entityClass;

        while (currentClass != null
                && currentClass != Object.class) {

            for (Field field : currentClass.getDeclaredFields()) {

                if (field.isAnnotationPresent(Translatable.class)) {
                    return true;
                }
            }

            currentClass = currentClass.getSuperclass();
        }

        return false;
    }
}