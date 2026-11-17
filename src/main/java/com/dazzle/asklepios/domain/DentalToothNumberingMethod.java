package com.dazzle.asklepios.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Named tooth-numbering method: display labels for the stable tooth ids
 * (Tooth1-Tooth32 / ToothA-ToothT). Labels are display metadata only; they are
 * never used as identifiers.
 */
@Entity
@Table(name = "dental_tooth_numbering_method")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DentalToothNumberingMethod extends AbstractAuditingEntity<Long> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 255)
    @Column(name = "description")
    private String description;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    /** JSON object {"Tooth1":"1","ToothA":"A",...}. Prefer {@link #getToothLabelMap()}. */
    @Column(name = "tooth_labels", columnDefinition = "text")
    private String toothLabels;

    @Transient
    public Map<String, String> getToothLabelMap() {
        if (toothLabels == null || toothLabels.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            return MAPPER.readValue(toothLabels, new TypeReference<LinkedHashMap<String, String>>() {});
        } catch (JsonProcessingException ex) {
            return new LinkedHashMap<>();
        }
    }

    @Transient
    public void setToothLabelMap(Map<String, String> labels) {
        if (labels == null || labels.isEmpty()) {
            this.toothLabels = null;
            return;
        }
        try {
            this.toothLabels = MAPPER.writeValueAsString(labels);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Tooth labels could not be serialized", ex);
        }
    }
}
