package com.dazzle.asklepios.web.rest.vm.coverage;

import com.dazzle.asklepios.domain.enumeration.CoverageClassCloneSection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.Set;

/**
 * Clones a coverage class into a new class of the same contract.
 *
 * @param name           name of the new class
 * @param sections       rule tabs to copy; tabs left out start empty
 * @param copyPolicyLink link the new class to the same insurance company policy
 */
public record CoverageClassCloneVM(
        @NotBlank
        @Size(max = 100)
        String name,
        Set<CoverageClassCloneSection> sections,
        Boolean copyPolicyLink
) implements Serializable {

    public boolean includes(CoverageClassCloneSection section) {
        return sections != null && sections.contains(section);
    }
}
