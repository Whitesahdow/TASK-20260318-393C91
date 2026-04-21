package com.busapp.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

@Service
public class TemplateProcessorService {
    private final ObjectMapper objectMapper;

    public TemplateProcessorService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public RawInput parseTemplate(TemplateImportRequest request) {
        if (request == null || request.getPayload() == null || request.getPayload().isBlank()) {
            throw new ValidationException("Template payload is required.");
        }

        String templateType = request.getTemplateType() == null ? "JSON" : request.getTemplateType().toUpperCase(Locale.ROOT);
        Map<String, String> mappings = request.getMappings() == null ? Map.of() : request.getMappings();
        Map<String, String> values = "HTML".equals(templateType)
                ? extractFromHtml(request.getPayload(), mappings)
                : extractFromJson(request.getPayload());
        return mapToRawInput(values, mappings);
    }

    private Map<String, String> extractFromJson(String payload) {
        try {
            Map<String, Object> data = objectMapper.readValue(payload, new TypeReference<>() {});
            Map<String, String> values = new HashMap<>();
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                values.put(entry.getKey(), entry.getValue() == null ? null : String.valueOf(entry.getValue()));
            }
            return values;
        } catch (Exception ex) {
            throw new ValidationException("Invalid JSON template payload.");
        }
    }

    private Map<String, String> extractFromHtml(String payload, Map<String, String> mappings) {
        Document doc = Jsoup.parse(payload);
        Map<String, String> values = new HashMap<>();
        for (String sourceKey : mappings.keySet()) {
            String value = doc.select("[data-key=" + sourceKey + "]").text();
            if (value == null || value.isBlank()) {
                value = doc.select("#" + sourceKey).text();
            }
            if (value == null || value.isBlank()) {
                value = doc.select(sourceKey).text();
            }
            values.put(sourceKey, value);
        }
        return values;
    }

    private RawInput mapToRawInput(Map<String, String> values, Map<String, String> mappings) {
        RawInput input = new RawInput();
        if (mappings.isEmpty()) {
            input.setName(values.get("name"));
            input.setAddress(values.get("address"));
            input.setResidentialArea(values.get("residentialArea"));
            input.setApartmentType(values.get("apartmentType"));
            input.setArea(parseDouble(values.get("area")));
            input.setUnit(values.get("unit"));
            input.setPrice(values.get("price"));
            return input;
        }

        for (Map.Entry<String, String> entry : mappings.entrySet()) {
            String sourceKey = entry.getKey();
            String targetField = entry.getValue();
            String val = values.get(sourceKey);
            if (targetField == null) {
                continue;
            }
            switch (targetField) {
                case "name" -> input.setName(val);
                case "address" -> input.setAddress(val);
                case "residentialArea" -> input.setResidentialArea(val);
                case "apartmentType" -> input.setApartmentType(val);
                case "area" -> input.setArea(parseDouble(val));
                case "unit" -> input.setUnit(val);
                case "price" -> input.setPrice(val);
                default -> { }
            }
        }
        return input;
    }

    private Double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
