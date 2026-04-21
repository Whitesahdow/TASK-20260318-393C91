package com.busapp.service;

import com.busapp.repository.CleaningRuleRepository;
import org.springframework.stereotype.Service;

@Service
public class ConfigService {
    private final CleaningRuleRepository cleaningRuleRepository;

    public ConfigService(CleaningRuleRepository cleaningRuleRepository) {
        this.cleaningRuleRepository = cleaningRuleRepository;
    }

    public double getFrequencyWeight() {
        return getWeight("SEARCH_FREQUENCY_WEIGHT", 0.7);
    }

    public double getPopularityWeight() {
        return getWeight("SEARCH_POPULARITY_WEIGHT", 0.3);
    }

    private double getWeight(String key, double fallback) {
        return cleaningRuleRepository.findByRuleKey(key)
                .filter(rule -> rule.isEnabled())
                .map(rule -> {
                    try {
                        return Double.parseDouble(rule.getRuleValue());
                    } catch (Exception ex) {
                        return fallback;
                    }
                })
                .orElse(fallback);
    }
}
