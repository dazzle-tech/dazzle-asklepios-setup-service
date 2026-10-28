package com.dazzle.asklepios.web.rest.vm.ambientScribe;

import com.dazzle.asklepios.domain.enumeration.AmbientAudioRetention;

public record AmbientScribeAudioRetentionVM(Long facilityId, AmbientAudioRetention audioRetention) {}
