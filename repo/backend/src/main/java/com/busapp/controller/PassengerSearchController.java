package com.busapp.controller;

import com.busapp.service.SearchResultDTO;
import com.busapp.service.SearchService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/v1")
public class PassengerSearchController {
    private final SearchService searchService;

    public PassengerSearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search/autocomplete")
    public ResponseEntity<List<SearchResultDTO>> autocomplete(@RequestParam("query") String query) {
        return ResponseEntity.ok(searchService.getAutocomplete(query));
    }

    @GetMapping("/search/results")
    public ResponseEntity<List<SearchResultDTO>> results(@RequestParam("query") String query) {
        return ResponseEntity.ok(searchService.getResults(query));
    }

    @GetMapping("/stops/{stopId}/metadata")
    public ResponseEntity<com.busapp.model.BusStop> getStopMetadata(@PathVariable Long stopId) {
        return ResponseEntity.ok(searchService.getMetadata(stopId));
    }
}

