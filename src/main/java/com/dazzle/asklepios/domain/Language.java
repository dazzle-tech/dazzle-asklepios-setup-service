package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.Direction;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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


    @Enumerated(EnumType.STRING)
    @Column(name = "direction")
    private Direction direction;

    @Size(max = 255)
    @Column(name = "details", length = 255)
    private String details;

}
