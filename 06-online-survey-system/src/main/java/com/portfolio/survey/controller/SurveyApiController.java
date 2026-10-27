package com.portfolio.survey.controller;

import com.portfolio.survey.dto.SurveyAnalyticsDto;
import com.portfolio.survey.service.SurveyAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/surveys")
public class SurveyApiController {

    private final SurveyAnalyticsService analyticsService;

    public SurveyApiController(SurveyAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<SurveyAnalyticsDto> getSurveyStats(@PathVariable("id") Long id) {
        SurveyAnalyticsDto stats = analyticsService.getAnalyticsForSurvey(id);
        return ResponseEntity.ok(stats);
    }
}
