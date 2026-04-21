package com.busapp;

import com.busapp.repository.SearchRepository;
import com.busapp.service.ConfigService;
import com.busapp.service.SearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class SearchRankingTest {
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
}
