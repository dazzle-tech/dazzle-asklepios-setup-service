package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DiscountCode;
import com.dazzle.asklepios.domain.DiscountCodeAllowedUser;
import com.dazzle.asklepios.domain.User;
import com.dazzle.asklepios.repository.DiscountCodeAllowedUserRepository;
import com.dazzle.asklepios.repository.DiscountCodeRepository;
import com.dazzle.asklepios.repository.UserRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import com.dazzle.asklepios.web.rest.vm.discountCode.DiscountCodeAllowedUserVM;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DiscountCodeAllowedUserService {

    private static final String ENTITY_NAME = "discountCodeAllowedUser";

    private final DiscountCodeAllowedUserRepository allowedUserRepository;
    private final DiscountCodeRepository discountCodeRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<DiscountCodeAllowedUserVM> findByDiscountCodeId(Long discountCodeId) {
        requireDiscountCode(discountCodeId);
        return allowedUserRepository.findByDiscountCode_IdOrderByIdAsc(discountCodeId)
                .stream()
                .map(DiscountCodeAllowedUserVM::ofEntity)
                .toList();
    }

    public List<DiscountCodeAllowedUserVM> replace(Long discountCodeId, List<Long> userIds) {
        DiscountCode discountCode = requireDiscountCode(discountCodeId);
        List<Long> distinctUserIds = userIds.stream()
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .toList();

        List<User> users = distinctUserIds.isEmpty()
                ? List.of()
                : userRepository.findAllById(distinctUserIds);

        if (users.size() != distinctUserIds.size()) {
            throw new BadRequestAlertException(
                    "One or more selected users were not found",
                    ENTITY_NAME,
                    "user.notfound"
            );
        }

        List<DiscountCodeAllowedUser> existing =
                allowedUserRepository.findByDiscountCode_IdOrderByIdAsc(discountCodeId);
        Set<Long> wanted = new HashSet<>(distinctUserIds);

        List<DiscountCodeAllowedUser> removed = existing.stream()
                .filter(row -> row.getUser() == null || !wanted.contains(row.getUser().getId()))
                .toList();
        if (!removed.isEmpty()) {
            allowedUserRepository.deleteAll(removed);
            allowedUserRepository.flush();
        }

        Set<Long> alreadyAssigned = new HashSet<>();
        for (DiscountCodeAllowedUser row : existing) {
            if (row.getUser() != null && wanted.contains(row.getUser().getId())) {
                alreadyAssigned.add(row.getUser().getId());
            }
        }

        for (User user : users) {
            if (alreadyAssigned.contains(user.getId())) {
                continue;
            }
            allowedUserRepository.save(DiscountCodeAllowedUser.builder()
                    .discountCode(discountCode)
                    .user(user)
                    .build());
        }

        return allowedUserRepository.findByDiscountCode_IdOrderByIdAsc(discountCodeId)
                .stream()
                .map(DiscountCodeAllowedUserVM::ofEntity)
                .toList();
    }

    private DiscountCode requireDiscountCode(Long discountCodeId) {
        return discountCodeRepository.findById(discountCodeId)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Discount code not found with id " + discountCodeId,
                        ENTITY_NAME,
                        "notfound"
                ));
    }
}
