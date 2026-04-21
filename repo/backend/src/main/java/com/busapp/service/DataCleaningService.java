package com.busapp.service;

import com.busapp.model.StopVersion;
import com.busapp.model.ImportAuditLog;
import com.busapp.repository.CleaningRuleRepository;
import com.busapp.repository.ImportAuditLogRepository;
import com.busapp.repository.StopVersionRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataCleaningService {
    private static final Logger log = LoggerFactory.getLogger(DataCleaningService.class);
    private final StopVersionRepository stopVersionRepository;
    private final CleaningRuleRepository cleaningRuleRepository;
    private final ImportAuditLogRepository importAuditLogRepository;

    public DataCleaningService(
            StopVersionRepository stopVersionRepository,
            CleaningRuleRepository cleaningRuleRepository,
            ImportAuditLogRepository importAuditLogRepository
    ) {
        this.stopVersionRepository = stopVersionRepository;
        this.cleaningRuleRepository = cleaningRuleRepository;
        this.importAuditLogRepository = importAuditLogRepository;
    }

    @Transactional
    public StopVersion importAndPersist(RawInput input, String sourceType) {
        StopVersion version = cleanAndTransform(input);
        String stopName = version.getStopName();
        Optional<StopVersion> latest = stopVersionRepository.findTopByStopNameOrderByVersionNumberDesc(stopName);
        int nextVersion = stopVersionRepository
                .findTopByStopNameOrderByVersionNumberDesc(stopName)
                .map(existing -> existing.getVersionNumber() + 1)
                .orElse(1);
        version.setVersionNumber(nextVersion);
        StopVersion saved = stopVersionRepository.save(version);
        persistAuditLog(saved, latest.orElse(null), sourceType);
        return saved;
    }

    @Transactional
    public StopVersion importAndPersist(RawInput input) {
        return importAndPersist(input, "JSON");
    }

    public List<StopVersion> recentAuditLogs() {
        return stopVersionRepository.findTop20ByOrderByImportedAtDesc();
    }

    public List<StopVersion> historyByStopName(String stopName) {
        return stopVersionRepository.findByStopNameOrderByVersionNumberDesc(stopName);
    }

    public List<ImportAuditLog> importAuditHistory() {
        return importAuditLogRepository.findTop50ByOrderByTimestampDesc();
    }

    public StopVersion cleanAndTransform(RawInput input) {
        RawInput safeInput = input == null ? new RawInput() : input;
        StopVersion version = new StopVersion();
        version.setStopName(defaultString(safeInput.getName(), "NULL"));
        version.setAddress(defaultString(safeInput.getAddress(), "NULL"));
        version.setResidentialArea(defaultString(safeInput.getResidentialArea(), "NULL"));
        version.setApartmentType(defaultString(safeInput.getApartmentType(), "NULL"));

        if (safeInput.getArea() != null) {
            version.setAreaSqm(convertToSqm(safeInput.getArea(), safeInput.getUnit()));
        } else {
            log.warn("[Audit] Missing area for stop: {}, source logged", version.getStopName());
            version.setAreaSqm(null);
        }

        version.setPriceYuanMonth(normalizePrice(safeInput.getPrice()));
        version.setRawSource(safeInput.toString());
        version.setImportedAt(LocalDateTime.now());
        version.setVersionNumber(1);
        return version;
    }

    private Double convertToSqm(Double val, String unit) {
        if (val == null) {
            return null;
        }
        if (unit != null && "sqft".equalsIgnoreCase(unit.trim())) {
            return val * readDoubleRule("SQFT_TO_SQM_FACTOR", 0.092903);
        }
        return val;
    }

    private Double normalizePrice(String rawPrice) {
        if (rawPrice == null || rawPrice.isBlank()) {
            return null;
        }
        String cleaned = rawPrice.toLowerCase(Locale.ROOT)
                .replace(readStringRule("PRICE_TARGET_SUFFIX", "yuan/month").toLowerCase(Locale.ROOT), "")
                .replace("yuan", "")
                .replace("/month", "")
                .replace(",", "")
                .trim();
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException exception) {
            log.warn("[Audit] Invalid price format for source value: {}", rawPrice);
            return null;
        }
    }

    private String defaultString(String value, String fallback) {
        String nullFallback = readStringRule("NULL_FALLBACK", fallback);
        if (value == null || value.isBlank()) {
            return nullFallback;
        }
        return value.trim();
    }

    private String readStringRule(String key, String fallback) {
        return cleaningRuleRepository.findByRuleKey(key)
                .filter(rule -> rule.isEnabled() && rule.getRuleValue() != null && !rule.getRuleValue().isBlank())
                .map(rule -> rule.getRuleValue())
                .orElse(fallback);
    }

    private double readDoubleRule(String key, double fallback) {
        String value = readStringRule(key, String.valueOf(fallback));
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private void persistAuditLog(StopVersion current, StopVersion previous, String sourceType) {
        List<String> changes = new ArrayList<>();
        if (current.getAreaSqm() != null && current.getRawSource().contains("sqft")) {
            changes.add("Converted area from sqft to sqm.");
        }
        if (current.getAreaSqm() == null) {
            changes.add("Area missing -> stored as NULL.");
        }
        if (previous != null && previous.getPriceYuanMonth() != null && current.getPriceYuanMonth() != null
                && !previous.getPriceYuanMonth().equals(current.getPriceYuanMonth())) {
            changes.add("Price changed from " + previous.getPriceYuanMonth() + " to " + current.getPriceYuanMonth() + ".");
        }
        if (changes.isEmpty()) {
            changes.add("Standardized values applied.");
        }

        ImportAuditLog audit = new ImportAuditLog();
        audit.setTimestamp(LocalDateTime.now());
        audit.setTraceId(MDC.get("traceId") == null ? "N/A" : MDC.get("traceId"));
        audit.setResult(current.getAreaSqm() == null ? "Partial" : "Success");
        audit.setChangesMade(String.join(" ", changes));
        audit.setStopName(current.getStopName());
        audit.setSourceType(sourceType == null ? "UNKNOWN" : sourceType);
        importAuditLogRepository.save(audit);
    }
}
