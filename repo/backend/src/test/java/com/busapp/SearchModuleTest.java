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
        
        com.busapp.model.BusStop stop1 = new com.busapp.model.BusStop();
        stop1.setId(1L);
        stop1.setNameEn("Central Station");
        
        com.busapp.model.BusRoute route1 = new com.busapp.model.BusRoute();
        route1.setRouteNumber("1A");
        
        com.busapp.model.RouteStopMapping mapping1 = new com.busapp.model.RouteStopMapping();
        mapping1.setStop(stop1);
        mapping1.setRoute(route1);
        
        com.busapp.model.BusRoute route2 = new com.busapp.model.BusRoute();
        route2.setRouteNumber("2B");
        
        com.busapp.model.RouteStopMapping mapping2 = new com.busapp.model.RouteStopMapping();
        mapping2.setStop(stop1);
        mapping2.setRoute(route2);

        when(searchRepository.findByStop_NameEnContainingIgnoreCaseOrStop_PinyinInitialsContainingIgnoreCase("Central", "Central"))
            .thenReturn(List.of(mapping1, mapping2));
            
        when(configService.getWeight("FREQ_WEIGHT", 0.7)).thenReturn(0.7);
        when(configService.getWeight("POP_WEIGHT", 0.3)).thenReturn(0.3);

        List<SearchResultDTO> results = searchService.getAutocomplete("Central");
        assertEquals(1, results.size(), "Results should be deduplicated by stop name");
        assertEquals("Central Station", results.get(0).getStopName());
        assertTrue(results.get(0).getRouteNumber().contains("1A"));
        assertTrue(results.get(0).getRouteNumber().contains("2B"));
    }
}
