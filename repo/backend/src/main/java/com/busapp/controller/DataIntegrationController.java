package com.busapp.controller;

import com.busapp.model.CleaningRule;
import com.busapp.model.FieldDictionaryEntry;
import com.busapp.model.ImportAuditLog;
import com.busapp.model.StopVersion;
import com.busapp.service.AdminConfigService;
import com.busapp.service.ConfigEntryRequest;
import com.busapp.service.DataCleaningService;
import com.busapp.service.RawInput;
import com.busapp.service.TemplateImportRequest;
import com.busapp.service.TemplateProcessorService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/admin/stops")
public class DataIntegrationController {
    private final DataCleaningService dataCleaningService;
    private final TemplateProcessorService templateProcessorService;
    private final AdminConfigService adminConfigService;
    private final ObjectMapper objectMapper;

    public DataIntegrationController(
            DataCleaningService dataCleaningService,
            TemplateProcessorService templateProcessorService,
            AdminConfigService adminConfigService,
            ObjectMapper objectMapper
    ) {
        this.dataCleaningService = dataCleaningService;
        this.templateProcessorService = templateProcessorService;
        this.adminConfigService = adminConfigService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/import")
    public ResponseEntity<StopVersion> importStop(@RequestBody(required = false) RawInput input) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dataCleaningService.importAndPersist(input));
    }

    @PostMapping("/import-json")
    public ResponseEntity<StopVersion> importStopJson(@RequestBody String jsonPayload) throws JsonProcessingException {
        RawInput input = objectMapper.readValue(jsonPayload, RawInput.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(dataCleaningService.importAndPersist(input, "JSON"));
    }

    @PostMapping("/import-template")
    public ResponseEntity<StopVersion> importTemplate(@RequestBody TemplateImportRequest request) {
        RawInput input = templateProcessorService.parseTemplate(request);
        String sourceType = request.getTemplateType() == null ? "JSON" : request.getTemplateType().toUpperCase();
        return ResponseEntity.status(HttpStatus.CREATED).body(dataCleaningService.importAndPersist(input, sourceType));
    }

    @GetMapping("/audit")
    public ResponseEntity<List<StopVersion>> recentAudit() {
        return ResponseEntity.ok(dataCleaningService.recentAuditLogs());
    }

    @GetMapping("/history/{stopName}")
    public ResponseEntity<List<StopVersion>> history(@PathVariable String stopName) {
        return ResponseEntity.ok(dataCleaningService.historyByStopName(stopName));
    }

    @GetMapping("/audit/imports")
    public ResponseEntity<List<ImportAuditLog>> importAudits() {
        return ResponseEntity.ok(dataCleaningService.importAuditHistory());
    }

    @GetMapping("/config/dictionaries")
    public ResponseEntity<List<FieldDictionaryEntry>> dictionaries() {
        return ResponseEntity.ok(adminConfigService.listDictionaries());
    }

    @PutMapping("/config/dictionaries")
    public ResponseEntity<List<FieldDictionaryEntry>> saveDictionaries(@RequestBody List<ConfigEntryRequest> entries) {
        return ResponseEntity.ok(adminConfigService.saveDictionaries(entries));
    }

    @GetMapping("/config/rules")
    public ResponseEntity<List<CleaningRule>> rules() {
        return ResponseEntity.ok(adminConfigService.listRules());
    }

    @PutMapping("/config/rules")
    public ResponseEntity<List<CleaningRule>> saveRules(@RequestBody List<ConfigEntryRequest> entries) {
        return ResponseEntity.ok(adminConfigService.saveRules(entries));
    }
}
