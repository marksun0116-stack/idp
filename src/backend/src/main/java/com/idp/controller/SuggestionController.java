package com.idp.controller;

import com.idp.service.DecisionSuggestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public/suggestions")
public class SuggestionController {

    private final DecisionSuggestionService suggestionService;

    public SuggestionController(DecisionSuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }

    /**
     * GET /api/suggestions/thesis — Get thesis suggestions
     */
    @GetMapping("/thesis")
    public ResponseEntity<Map<String, List<String>>> getThesisSuggestions() {
        return ResponseEntity.ok(Map.of("suggestions", suggestionService.getThesisSuggestions()));
    }

    /**
     * GET /api/suggestions/evidence — Get evidence suggestions
     */
    @GetMapping("/evidence")
    public ResponseEntity<Map<String, List<String>>> getEvidenceSuggestions() {
        return ResponseEntity.ok(Map.of("suggestions", suggestionService.getEvidenceSuggestions()));
    }

    /**
     * GET /api/suggestions/risks — Get risks suggestions
     */
    @GetMapping("/risks")
    public ResponseEntity<Map<String, List<String>>> getRisksSuggestions() {
        return ResponseEntity.ok(Map.of("suggestions", suggestionService.getRisksSuggestions()));
    }

    /**
     * GET /api/suggestions/all — Get all suggestions
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllSuggestions() {
        return ResponseEntity.ok(Map.of(
            "thesis", suggestionService.getThesisSuggestions(),
            "evidence", suggestionService.getEvidenceSuggestions(),
            "risks", suggestionService.getRisksSuggestions()
        ));
    }
}
