package com.example.SmarttuneBackend.controller;

import com.example.SmarttuneBackend.dto.ChansonResponse;
import com.example.SmarttuneBackend.entities.Artiste;
import com.example.SmarttuneBackend.metier.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/artistes")
    public ResponseEntity<List<Artiste>> searchArtistes(@RequestParam("query") String query) {
        return ResponseEntity.ok(searchService.searchArtistes(query));
    }

    @GetMapping("/chansons")
    public ResponseEntity<List<ChansonResponse>> searchChansons(@RequestParam("query") String query) {
        return ResponseEntity.ok(searchService.searchChansons(query));
    }
}