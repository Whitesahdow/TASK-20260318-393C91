package com.busapp.service;

import com.busapp.repository.SearchRepository;
import com.busapp.repository.SearchRowProjection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class SearchService {
    private final SearchRepository searchRepository;
    private final ConfigService configService;

    public SearchService(SearchRepository searchRepository, ConfigService configService) {
        this.searchRepository = searchRepository;
        this.configService = configService;
    }

    public List<SearchResultDTO> getAutocomplete(String query) {
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

    public double calculateScore(double frequency, double popularity, double fWeight, double pWeight) {
        return (frequency * fWeight) + (popularity * pWeight);
    }
}
