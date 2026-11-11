package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DiscountCode;
import com.dazzle.asklepios.domain.User;
import com.dazzle.asklepios.repository.DiscountCodeAllowedUserRepository;
import com.dazzle.asklepios.repository.DiscountCodeRepository;
import com.dazzle.asklepios.repository.UserRepository;
import com.dazzle.asklepios.service.dto.discountCode.DiscountCodeValidateDTO;
import com.dazzle.asklepios.service.dto.discountCode.DiscountCodeVerifyUserDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.discountCode.DiscountCodeVerifiedUserVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class DiscountCodeAuthorizationService {

    private static final Logger LOG = LoggerFactory.getLogger(DiscountCodeAuthorizationService.class);

    private static final String ENTITY_NAME = "discountCode";

    private static final Duration AUTHORIZATION_TTL = Duration.ofMinutes(15);

    private static final String DUMMY_PASSWORD_HASH =
            new BCryptPasswordEncoder().encode("discount-code-user-missing");

    private final UserRepository userRepository;
    private final DiscountCodeRepository discountCodeRepository;
    private final DiscountCodeAllowedUserRepository allowedUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final byte[] tokenKey;

    public DiscountCodeAuthorizationService(
            UserRepository userRepository,
            DiscountCodeRepository discountCodeRepository,
            DiscountCodeAllowedUserRepository allowedUserRepository,
            PasswordEncoder passwordEncoder,
            @Value("${setup.security.authentication.jwt.base64-secret}") String tokenSecret
    ) {
        this.userRepository = userRepository;
        this.discountCodeRepository = discountCodeRepository;
        this.allowedUserRepository = allowedUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenKey = tokenSecret.getBytes(StandardCharsets.UTF_8);
    }

    public DiscountCodeVerifiedUserVM verifyUser(DiscountCodeVerifyUserDTO dto) {
        String login = dto.login().trim();
        LOG.debug("Request to verify discount-code user login={}", login);

        User user = findUser(login).orElse(null);
        String hash = user != null && user.getPassword() != null && !user.getPassword().isBlank()
                ? user.getPassword()
                : DUMMY_PASSWORD_HASH;

        boolean passwordMatches;
        try {
            passwordMatches = passwordEncoder.matches(dto.password(), hash);
        } catch (IllegalArgumentException ex) {
            passwordMatches = false;
        }

        if (user == null || !user.isActivated() || !passwordMatches) {
            throw new BadRequestAlertException(
                    "Invalid username or password.",
                    ENTITY_NAME,
                    "invalidcredentials"
            );
        }

        long expiresAt = System.currentTimeMillis() + AUTHORIZATION_TTL.toMillis();
        return new DiscountCodeVerifiedUserVM(
                user.getId(),
                user.getLogin(),
                user.getFirstName(),
                user.getLastName(),
                sign(user.getId(), expiresAt)
        );
    }

    public DiscountCode validate(DiscountCodeValidateDTO dto) {
        Long authorizedUserId = requireAuthorizedUser(dto.authorizationToken());
        if (!authorizedUserId.equals(dto.userId())) {
            throw new BadRequestAlertException(
                    "Discount authorization does not match this user. Verify the user again.",
                    ENTITY_NAME,
                    "authorizationmismatch"
            );
        }

        String code = dto.code().trim();
        DiscountCode discountCode = discountCodeRepository
                .findByFacility_IdAndCodeIgnoreCase(dto.facilityId(), code)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Discount code is not valid for this facility.",
                        ENTITY_NAME,
                        "invalid"
                ));

        if (!Boolean.TRUE.equals(discountCode.getIsActive())) {
            throw new BadRequestAlertException(
                    "Discount code is inactive.",
                    ENTITY_NAME,
                    "inactive"
            );
        }

        if (!allowedUserRepository.existsByDiscountCode_IdAndUser_Id(discountCode.getId(), authorizedUserId)) {
            throw new BadRequestAlertException(
                    "This user is not allowed to use this discount code.",
                    ENTITY_NAME,
                    "notallowed"
            );
        }

        LOG.debug(
                "Discount code {} authorized for userId={} facilityId={}",
                discountCode.getCode(),
                authorizedUserId,
                dto.facilityId()
        );
        return discountCode;
    }

    private Optional<User> findUser(String login) {
        return userRepository.findByLoginIgnoreCase(login);
    }

    private Long requireAuthorizedUser(String token) {
        if (token == null || token.isBlank()) {
            throw new BadRequestAlertException(
                    "Discount authorization is missing. Verify the user again.",
                    ENTITY_NAME,
                    "authorization"
            );
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw invalidAuthorization();
        }

        long userId;
        long expiresAt;
        try {
            userId = Long.parseLong(parts[0]);
            expiresAt = Long.parseLong(parts[1]);
        } catch (NumberFormatException ex) {
            throw invalidAuthorization();
        }

        if (expiresAt < System.currentTimeMillis()) {
            throw new BadRequestAlertException(
                    "Discount authorization expired. Verify the user again.",
                    ENTITY_NAME,
                    "authorizationexpired"
            );
        }

        String expected = mac(parts[0] + "." + parts[1]);
        byte[] actualBytes = parts[2].getBytes(StandardCharsets.UTF_8);
        byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(expectedBytes, actualBytes)) {
            throw invalidAuthorization();
        }
        return userId;
    }

    private String sign(Long userId, long expiresAt) {
        String payload = userId + "." + expiresAt;
        return payload + "." + mac(payload);
    }

    private String mac(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(tokenKey, "HmacSHA256"));
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign discount authorization.", ex);
        }
    }

    private BadRequestAlertException invalidAuthorization() {
        return new BadRequestAlertException(
                "Discount authorization is not valid. Verify the user again.",
                ENTITY_NAME,
                "authorization"
        );
    }
}
