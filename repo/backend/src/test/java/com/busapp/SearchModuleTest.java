package com.busapp;

import com.busapp.repository.SearchRepository;
import com.busapp.service.ConfigService;
import com.busapp.service.SearchResultDTO;
import com.busapp.service.SearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class SearchModuleTest {
    @Mock
    private SearchRepository searchRepository;
    @Mock
    private ConfigService configService;

    @Test
    void verifyWeightedRankingScore() {
        SearchService rankingService = new SearchService(searchRepository, configService);
        double score = rankingService.calculateScore(10, 50, 0.7, 0.3);
        assertEquals(22.0, score);
    }

    @Test
    void whenQueryTooShort_thenReturnEmpty() {
        SearchService searchService = new SearchService(searchRepository, configService);
        List<SearchResultDTO> results = searchService.getAutocomplete("a");
        assertTrue(results.isEmpty(), "Autocomplete should only trigger at 2+ characters.");
    }

    @Test
    void testDeduplicationOfSearchResults() {
        SearchService searchService = new SearchService(searchRepository, configService);
        
        com.busapp.repository.SearchRowProjection row1 = org.mockito.Mockito.mock(com.busapp.repository.SearchRowProjection.class);
        org.mockito.Mockito.when(row1.getStopId()).thenReturn(1L);
        org.mockito.Mockito.when(row1.getStopName()).thenReturn("Central Station");
        org.mockito.Mockito.when(row1.getRouteNumber()).thenReturn("1A");
        org.mockito.Mockito.when(row1.getScore()).thenReturn(15.0);

        com.busapp.repository.SearchRowProjection row2 = org.mockito.Mockito.mock(com.busapp.repository.SearchRowProjection.class);
        org.mockito.Mockito.when(row2.getStopId()).thenReturn(1L);
        
        when(configService.getFrequencyWeight()).thenReturn(0.7);
        when(configService.getPopularityWeight()).thenReturn(0.3);

        when(searchRepository.searchStops("Central", 0.7, 0.3))
            .thenReturn(List.of(row1, row2));

        List<SearchResultDTO> results = searchService.getAutocomplete("Central");
        assertEquals(1, results.size(), "Results should be deduplicated by stop id");
        assertEquals("Central Station", results.get(0).getStopName());
        assertTrue(results.get(0).getRouteNumber().contains("1A"));
    }
}
