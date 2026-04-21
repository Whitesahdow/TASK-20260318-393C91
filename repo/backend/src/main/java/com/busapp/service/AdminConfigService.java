package com.busapp.service;

import com.busapp.model.CleaningRule;
import com.busapp.model.FieldDictionaryEntry;
import com.busapp.repository.CleaningRuleRepository;
import com.busapp.repository.FieldDictionaryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminConfigService {
    private final FieldDictionaryRepository fieldDictionaryRepository;
    private final CleaningRuleRepository cleaningRuleRepository;

    public AdminConfigService(
            FieldDictionaryRepository fieldDictionaryRepository,
            CleaningRuleRepository cleaningRuleRepository
    ) {
        this.fieldDictionaryRepository = fieldDictionaryRepository;
        this.cleaningRuleRepository = cleaningRuleRepository;
    }

    @Transactional
    public void ensureDefaults() {
        upsertDictionary("AREA_UNIT_SUFFIX", "㎡");
        upsertDictionary("PRICE_UNIT_SUFFIX", "yuan/month");
        upsertRule("SQFT_TO_SQM_FACTOR", "0.092903", true);
        upsertRule("NULL_FALLBACK", "NULL", true);
        upsertRule("PRICE_TARGET_SUFFIX", "yuan/month", true);
        upsertRule("SEARCH_FREQUENCY_WEIGHT", "0.7", true);
        upsertRule("SEARCH_POPULARITY_WEIGHT", "0.3", true);
    }

    public List<FieldDictionaryEntry> listDictionaries() {
        ensureDefaults();
        return fieldDictionaryRepository.findAll();
    }

    public List<CleaningRule> listRules() {
        ensureDefaults();
        return cleaningRuleRepository.findAll();
    }

    @Transactional
    public List<FieldDictionaryEntry> saveDictionaries(List<ConfigEntryRequest> entries) {
        ensureDefaults();
        for (ConfigEntryRequest entry : entries) {
            upsertDictionary(entry.getKey(), entry.getValue());
        }
        return fieldDictionaryRepository.findAll();
    }

    @Transactional
    public List<CleaningRule> saveRules(List<ConfigEntryRequest> entries) {
        ensureDefaults();
        for (ConfigEntryRequest entry : entries) {
            upsertRule(entry.getKey(), entry.getValue(), entry.getEnabled() == null || entry.getEnabled());
        }
        return cleaningRuleRepository.findAll();
    }

    private void upsertDictionary(String key, String value) {
        FieldDictionaryEntry entry = fieldDictionaryRepository.findByDictKey(key).orElseGet(FieldDictionaryEntry::new);
        entry.setDictKey(key);
        entry.setDictValue(value == null ? "" : value);
        fieldDictionaryRepository.save(entry);
    }

    private void upsertRule(String key, String value, boolean enabled) {
        CleaningRule rule = cleaningRuleRepository.findByRuleKey(key).orElseGet(CleaningRule::new);
        rule.setRuleKey(key);
        rule.setRuleValue(value == null ? "" : value);
        rule.setEnabled(enabled);
        cleaningRuleRepository.save(rule);
    }
}
