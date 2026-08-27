package com.dazzle.asklepios.web.rest.vm.nphiespayer;

import java.io.Serializable;
import java.util.List;

public record NphiesPayerTpasUpdateVM(List<Long> tpaIds) implements Serializable {}
