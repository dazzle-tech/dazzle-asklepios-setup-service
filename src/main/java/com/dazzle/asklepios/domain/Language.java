package com.dazzle.asklepios.domain;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Column;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Language {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "lang_key", length = 50, nullable = false, unique = true)
    private String langKey;

    @NotBlank
    @Size(max = 100)
    @Column(name = "lang_name", length = 100, nullable = false)
    private String langName;

    // Use string to match your Liquibase varchar(10). Accepts values like "LTR" / "RTL".
    @NotBlank
    @Size(max = 10)
    @Column(name = "direction", length = 10, nullable = false)
    private String direction;

    @Size(max = 255)
    @Column(name = "details", length = 255)
    private String details;

}
