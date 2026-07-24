package com.dazzle.asklepios.domain.enumeration.notification;

public enum RecipientRule {
    PATIENT_EMAIL,
    PATIENT_PHONE,

    PRACTITIONER_EMAIL,
    PRACTITIONER_PHONE,
    PRACTITIONER_USER,

    DEPARTMENT_USERS,
    PHYSICIAN_DEPARTMENT_USERS,
    CURRENT_USER,
    CURRENT_USER_PHONE,
    CREATED_BY_USER,
    CREATED_BY_USER_PHONE,

    DATA,
    STATIC
}
