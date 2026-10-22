package com.dazzle.asklepios.web.rest.vm.supportivedocument;

import java.util.List;

public record PayorSupportiveDocumentRequirementResponseVM(
        Long payorId,
        boolean configured,
        List<String> requirementCodes
) {
}
