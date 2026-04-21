package com.busapp.controller;

import com.busapp.infra.monitoring.PerformanceMonitor;
import com.busapp.model.NotificationTemplate;
import com.busapp.repository.NotificationTemplateRepository;
import com.busapp.service.ConfigEntryRequest;
import com.busapp.service.DataCleaningService;
import com.busapp.service.ValidationException;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/maintenance")
public class AdminMaintenanceController {
    private final DataCleaningService dataCleaningService;
    private final NotificationTemplateRepository templateRepository;
    private final PerformanceMonitor performanceMonitor;

    public AdminMaintenanceController(
            DataCleaningService dataCleaningService,
            NotificationTemplateRepository templateRepository,
            PerformanceMonitor performanceMonitor
    ) {
        this.dataCleaningService = dataCleaningService;
        this.templateRepository = templateRepository;
        this.performanceMonitor = performanceMonitor;
    }

    @GetMapping("/weights")
    public ResponseEntity<List<ConfigEntryRequest>> getWeights() {
        List<ConfigEntryRequest> rules = dataCleaningService.listRuleConfigs();
        List<ConfigEntryRequest> filtered = rules.stream()
                .filter(r -> "SEARCH_FREQUENCY_WEIGHT".equals(r.getKey()) || "SEARCH_POPULARITY_WEIGHT".equals(r.getKey()))
                .toList();
        return ResponseEntity.ok(filtered);
    }

    @PutMapping("/weights")
    public ResponseEntity<List<ConfigEntryRequest>> saveWeights(@RequestBody List<ConfigEntryRequest> entries) {
        for (ConfigEntryRequest entry : entries) {
            if (!"SEARCH_FREQUENCY_WEIGHT".equals(entry.getKey()) && !"SEARCH_POPULARITY_WEIGHT".equals(entry.getKey())) {
                throw new ValidationException("Only search weight keys are allowed.");
            }
        }
        return ResponseEntity.ok(dataCleaningService.saveRuleConfigs(entries));
    }

    @GetMapping("/templates")
    public ResponseEntity<List<NotificationTemplate>> templates() {
        return ResponseEntity.ok(templateRepository.findAll());
    }

    @PutMapping("/templates")
    public ResponseEntity<List<NotificationTemplate>> saveTemplates(@RequestBody List<NotificationTemplate> templates) {
        for (NotificationTemplate t : templates) {
            if (t.getTemplateKey() == null || t.getTemplateKey().isBlank()) {
                throw new ValidationException("Template key is required.");
            }
        }
        return ResponseEntity.ok(templateRepository.saveAll(templates));
    }

    @GetMapping("/monitor/simulate-p95")
    public ResponseEntity<String> simulateP95() {
        performanceMonitor.analyzeLatency(List.of(100L, 120L, 300L, 700L, 900L, 1200L));
        return ResponseEntity.ok("P95 simulation executed");
    }
}
