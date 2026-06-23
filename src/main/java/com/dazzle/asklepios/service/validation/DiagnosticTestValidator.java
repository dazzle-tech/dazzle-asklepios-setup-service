package com.dazzle.asklepios.service.validation;

import com.dazzle.asklepios.domain.enumeration.TestType;
import com.dazzle.asklepios.web.rest.vm.diagnostictest.DiagnosticTestCreateVM;
import com.dazzle.asklepios.web.rest.vm.diagnostictest.DiagnosticTestUpdateVM;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DiagnosticTestValidator implements ConstraintValidator<ValidDiagnosticTest, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) return true;

        TestType type = null;
        String modality = null;

        if (value instanceof DiagnosticTestCreateVM vm) {
            type = vm.type();
            modality = vm.modality();
        } else if (value instanceof DiagnosticTestUpdateVM vm) {
            type = vm.type();
            modality = vm.modality();
        }

        if (type == null) return true;

        if (requiresModality(type) && (modality == null || modality.isBlank())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "modality is mandatory when type is RADIOLOGY"
                    )
                    .addPropertyNode("modality")
                    .addConstraintViolation();

            return false;
        }

        return true;
    }

    private boolean requiresModality(TestType type) {
        return type == TestType.RADIOLOGY;
    }
}
