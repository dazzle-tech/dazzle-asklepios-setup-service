package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DiscountCodeAllowedUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscountCodeAllowedUserRepository extends JpaRepository<DiscountCodeAllowedUser, Long> {

    @EntityGraph(attributePaths = "user")
    List<DiscountCodeAllowedUser> findByDiscountCode_IdOrderByIdAsc(Long discountCodeId);

    boolean existsByDiscountCode_IdAndUser_Id(Long discountCodeId, Long userId);
}
