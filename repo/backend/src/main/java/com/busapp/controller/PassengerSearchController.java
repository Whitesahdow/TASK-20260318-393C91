package com.busapp.controller;

import com.busapp.service.SearchResultDTO;
import com.busapp.service.SearchService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/passenger")
public class PassengerSearchController {
    private final SearchService searchService;

    public PassengerSearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<SearchResultDTO>> search(@RequestParam("query") String query) {
        return ResponseEntity.ok(searchService.getAutocomplete(query));
    }
}
