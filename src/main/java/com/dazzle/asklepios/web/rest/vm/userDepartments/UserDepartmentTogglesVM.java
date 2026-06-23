package com.dazzle.asklepios.web.rest.vm.userDepartments;

public record UserDepartmentTogglesVM(
        Boolean isDefault,
        Boolean appointmentBookingAllowed
) {}