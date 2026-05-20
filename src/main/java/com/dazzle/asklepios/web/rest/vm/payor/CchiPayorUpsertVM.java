package com.dazzle.asklepios.web.rest.vm.payor;

import java.io.Serializable;

public record CchiPayorUpsertVM(
        String payerNphiesId,
        String payerId,
        String payerName,
        String tpaNphiesId
) implements Serializable {
}