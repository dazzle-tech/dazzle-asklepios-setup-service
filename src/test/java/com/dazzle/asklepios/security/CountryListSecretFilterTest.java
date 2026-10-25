package com.dazzle.asklepios.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CountryListSecretFilterTest {

    private static final String SECRET = "dev-patient-portal-secret";

    @Test
    void rejectsExternalActiveListWithoutSecret() throws Exception {
        CountryListSecretFilter filter = new CountryListSecretFilter(SECRET);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/setup/country/external/active");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            throw new AssertionError("filter chain should not continue");
        });

        assertEquals(401, response.getStatus());
    }

    @Test
    void rejectsBearerTokenWithoutSecret() throws Exception {
        CountryListSecretFilter filter = new CountryListSecretFilter(SECRET);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/setup/country/external/active");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            throw new AssertionError("filter chain should not continue");
        });

        assertEquals(401, response.getStatus());
    }

    @Test
    void allowsMatchingPortalSecret() throws Exception {
        CountryListSecretFilter filter = new CountryListSecretFilter(SECRET);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/setup/country/external/active");
        request.addHeader(CountryListSecretFilter.SECRET_HEADER, SECRET);
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean[] continued = { false };

        filter.doFilter(request, response, (req, res) -> continued[0] = true);

        assertTrue(continued[0]);
    }

    @Test
    void rejectsMobileConfigValueWithoutSecret() throws Exception {
        CountryListSecretFilter filter = new CountryListSecretFilter(SECRET);
        MockHttpServletRequest request = new MockHttpServletRequest(
            "GET",
            "/api/setup/mobile-app-config/value"
        );
        request.setQueryString("keys=SUPPORT_PHONE&keys=BANNER_IMAGE1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            throw new AssertionError("filter chain should not continue");
        });

        assertEquals(401, response.getStatus());
    }

    @Test
    void allowsMobileConfigValueWithPortalSecret() throws Exception {
        CountryListSecretFilter filter = new CountryListSecretFilter(SECRET);
        MockHttpServletRequest request = new MockHttpServletRequest(
            "GET",
            "/api/setup/mobile-app-config/value"
        );
        request.setQueryString("keys=SUPPORT_PHONE&keys=BANNER_IMAGE1");
        request.addHeader(CountryListSecretFilter.SECRET_HEADER, SECRET);
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean[] continued = { false };

        filter.doFilter(request, response, (req, res) -> continued[0] = true);

        assertTrue(continued[0]);
    }

    @Test
    void ignoresMobileConfigAdminList() throws Exception {
        CountryListSecretFilter filter = new CountryListSecretFilter(SECRET);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/setup/mobile-app-config");
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean[] continued = { false };

        filter.doFilter(request, response, (req, res) -> continued[0] = true);

        assertTrue(continued[0]);
    }

    @Test
    void ignoresExistingActiveCountryRoute() throws Exception {
        CountryListSecretFilter filter = new CountryListSecretFilter(SECRET);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/setup/country/active");
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean[] continued = { false };

        filter.doFilter(request, response, (req, res) -> continued[0] = true);

        assertTrue(continued[0]);
    }
}
