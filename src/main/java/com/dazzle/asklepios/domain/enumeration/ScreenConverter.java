package com.dazzle.asklepios.domain.enumeration;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ScreenConverter  extends GeneralConverter<Screen> {
    public ScreenConverter() {
        super(Screen.class);
    }
}
