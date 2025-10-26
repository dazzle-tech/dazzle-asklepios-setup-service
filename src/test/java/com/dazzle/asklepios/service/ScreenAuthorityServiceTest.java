package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.ScreenAuthority;
import com.dazzle.asklepios.repository.ScreenAuthorityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

class ScreenAuthorityServiceTest {

    @Mock
    private ScreenAuthorityRepository screenAuthorityRepository;

    @InjectMocks
    private ScreenAuthorityService screenAuthorityService;

    private ScreenAuthority screenAuthority;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        screenAuthority = ScreenAuthority.builder()
                .id(1L)
                .screen(null)
                .operation(null)
                .authorityName("TEST_AUTH")
                .build();
    }

    @Test
    void testGetAll() {
        List<ScreenAuthority> authorities = List.of(screenAuthority);
        when(screenAuthorityRepository.findAll()).thenReturn(authorities);

        List<ScreenAuthority> result = screenAuthorityService.getAll();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAuthorityName()).isEqualTo("TEST_AUTH");

        verify(screenAuthorityRepository, times(1)).findAll();
    }
}
