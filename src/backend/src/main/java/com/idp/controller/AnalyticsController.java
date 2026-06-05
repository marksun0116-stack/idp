package com.idp.controller;

import com.idp.dto.BehaviorScorecardResponse;
import com.idp.dto.DqsResponse;
import com.idp.service.AnalyticsService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/dqs")
    public DqsResponse dqs(Authentication authentication) {
        return analyticsService.dqs(authentication.getName());
    }

    @GetMapping("/behavior")
    public BehaviorScorecardResponse behavior(Authentication authentication) {
        return analyticsService.behavior(authentication.getName());
    }
}
