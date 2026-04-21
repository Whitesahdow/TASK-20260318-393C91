package com.busapp;

import com.busapp.repository.SearchRepository;
import com.busapp.service.ConfigService;
import com.busapp.service.SearchResultDTO;
import com.busapp.service.SearchService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class AutocompleteTest {
    @Mock
    private SearchRepository searchRepository;
    @Mock
    private ConfigService configService;

    @Test
    void whenQueryTooShort_thenReturnEmpty() {
        SearchService searchService = new SearchService(searchRepository, configService);
        List<SearchResultDTO> results = searchService.getAutocomplete("a");
        assertTrue(results.isEmpty(), "Autocomplete should only trigger at 2+ characters.");
    }
}
