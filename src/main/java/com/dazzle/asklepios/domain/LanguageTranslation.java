package com.dazzle.asklepios.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class LanguageTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "lang_key", length = 50, nullable = false)
    @NotBlank
    private String langKey;

    /*
     * Used for static translations such as:
     * button.save
     * header.patient
     * table.name
     */
    @Column(name = "translation_key", length = 150)
    private String translationKey;

    /*
     * Used for dynamic entity translations such as:
     * DEPARTMENT
     * SERVICE
     * CATEGORY
     */
    @Column(name = "resource_type", length = 100)
    private String resourceType;

    /*
     * Entity identifier or Enum value.
     *
     * Examples:
     * Department ID -> "15"
     * Enum value    -> "PENDING"
     */
    @Column(name = "resource_key", length = 150)
    private String resourceKey;

    /*
     * Name of the translated field.
     *
     * Examples:
     * name
     * description
     */
    @Column(name = "field_name", length = 100)
    private String fieldName;

    @Column(name = "enum_type", length = 150)
    private String enumType;


    @Column(name = "translation_text", columnDefinition = "text")
    private String translationText;

    @Column(name = "verified", nullable = false)
    @NotNull
    private Boolean verified = false;

    @Column(name = "translated", nullable = false)
    @NotNull
    private Boolean translated = false;
}