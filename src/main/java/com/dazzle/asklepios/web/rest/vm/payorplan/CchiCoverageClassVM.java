package com.dazzle.asklepios.web.rest.vm.payorplan;

import java.io.Serializable;

public record CchiCoverageClassVM(
        String type,
        String value,
        String name
) implements Serializable {
}