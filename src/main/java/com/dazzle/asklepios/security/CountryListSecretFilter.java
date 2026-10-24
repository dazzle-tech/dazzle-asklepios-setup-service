package com.dazzle.asklepios.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Authorizes external GET routes with {@code PATIENT_PORTAL_SECRET}.
 * These routes are not checked against a bearer token.
 * <ul>
 *   <li>{@code GET /api/setup/country/external/active}</li>
 *   <li>{@code GET /api/setup/mobile-app-config/value/{configKey}}</li>
 * </ul>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CountryListSecretFilter extends OncePerRequestFilter {

    public static final String SECRET_HEADER = "X-Portal-Secret";

    private static final Logger LOG = LoggerFactory.getLogger(CountryListSecretFilter.class);

    private static final String EXTERNAL_ACTIVE_COUNTRY_PATH = "/api/setup/country/external/active";

    private static final String MOBILE_APP_CONFIG_VALUE_PREFIX = "/api/setup/mobile-app-config/value/";

    private final String portalSecret;

    public CountryListSecretFilter(@Value("${setup.portal.secret:}") String portalSecret) {
        this.portalSecret = portalSecret == null ? "" : portalSecret;
        if (this.portalSecret.isBlank()) {
            LOG.warn(
                "setup.portal.secret is not set; external country and mobile app config value requests will reject every request"
            );
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !isSecretProtectedRequest(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        if (!secretMatches(request.getHeader(SECRET_HEADER))) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean secretMatches(String provided) {
        if (portalSecret.isBlank() || provided == null || provided.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(sha256(provided), sha256(portalSecret));
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private static boolean isSecretProtectedRequest(HttpServletRequest request) {
        if (!HttpMethod.GET.matches(request.getMethod())) {
            return false;
        }
        String path = requestPath(request);
        return EXTERNAL_ACTIVE_COUNTRY_PATH.equals(path) || path.startsWith(MOBILE_APP_CONFIG_VALUE_PREFIX);
    }

    private static String requestPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        return path;
    }
}
