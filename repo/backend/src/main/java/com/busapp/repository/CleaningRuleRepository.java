package com.busapp.repository;

import com.busapp.model.CleaningRule;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CleaningRuleRepository extends JpaRepository<CleaningRule, Long> {
    Optional<CleaningRule> findByRuleKey(String ruleKey);
}
