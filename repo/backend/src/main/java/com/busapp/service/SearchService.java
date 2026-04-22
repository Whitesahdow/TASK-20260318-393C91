package com.busapp.service;

import com.busapp.repository.SearchRepository;
import com.busapp.repository.SearchRowProjection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
public class SearchService {
    private static final Logger log = LoggerFactory.getLogger(SearchService.class);
    private final SearchRepository searchRepository;
    private final ConfigService configService;

    public SearchService(SearchRepository searchRepository, ConfigService configService) {
        this.searchRepository = searchRepository;
        this.configService = configService;
    }

    public List<SearchResultDTO> getAutocomplete(String query) {
        log.info("[Trace: {}] Processing autocomplete for query: {}", MDC.get("traceId"), query);
        if (query == null || query.trim().length() < 2) {
            return List.of();
        }

        double fWeight = configService.getFrequencyWeight();
        double pWeight = configService.getPopularityWeight();
        List<SearchRowProjection> rows = searchRepository.searchStops(query.trim(), fWeight, pWeight);

        Set<Long> seenStops = new HashSet<>();
        List<SearchResultDTO> result = new ArrayList<>();
        for (SearchRowProjection row : rows) {
            if (seenStops.contains(row.getStopId())) {
                continue;
            }
            seenStops.add(row.getStopId());
            SearchResultDTO dto = new SearchResultDTO();
            dto.setStopId(row.getStopId());
            dto.setStopName(row.getStopName());
            dto.setInitials(row.getInitials());
            dto.setRouteNumber(row.getRouteNumber());
            dto.setScore(row.getScore());
            dto.setPopularity(row.getPopularity());
            result.add(dto);
        }
        return result;
    }

    public List<SearchResultDTO> getResults(String query) {
        List<SearchResultDTO> results = getAutocomplete(query);
        results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        return results;
    }

    public com.busapp.model.BusStop getMetadata(Long stopId) {
        log.info("[Trace: {}] Processing metadata request for stopId: {}", MDC.get("traceId"), stopId);
        com.busapp.model.BusStop stop = searchRepository.findById(stopId)
                .orElseThrow(() -> new ValidationException("Stop not found: " + stopId));
        // Desensitize housing data
        if (stop.getHousingData() != null) {
            stop.setHousingData(MaskingUtils.mask(stop.getHousingData(), com.busapp.model.SensitivityLevel.LEVEL2));
        }
        return stop;
    }

    public double calculateScore(double frequency, double popularity, double fWeight, double pWeight) {
        return (frequency * fWeight) + (popularity * pWeight);
    }
}
