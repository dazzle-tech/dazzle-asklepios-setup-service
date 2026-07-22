package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.BillingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface BillingRuleRepository extends JpaRepository<BillingRule, Long> {

}