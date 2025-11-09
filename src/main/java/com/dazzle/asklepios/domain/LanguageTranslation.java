package com.dazzle.asklepios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private Long id;

    @Column(name = "lang_key", length = 50, nullable = false)
    @NotBlank
    private String langKey;

    @Column(name = "translation_key", length = 150, nullable = false)
    @NotBlank
    private String translationKey;


    @Column(name = "translation_text", columnDefinition = "text")
    private String translationText;

    @Column(name = "verified", nullable = false)
    @NotNull
    private Boolean verified = false;

    @Column(name = "translated", nullable = false)
    @NotNull
    private Boolean translated = false ;

   }
