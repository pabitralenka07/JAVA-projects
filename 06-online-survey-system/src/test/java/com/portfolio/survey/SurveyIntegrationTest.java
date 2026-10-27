package com.portfolio.survey;

import com.portfolio.survey.model.Survey;
import com.portfolio.survey.repository.SurveyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SurveyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SurveyRepository surveyRepository;

    @Test
    @DisplayName("GET / - Should display public portal with seeded surveys")
    void testPublicIndex() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("surveys"))
                .andExpect(content().string(containsString("SurveySphere")));
    }

    @Test
    @DisplayName("GET /survey/{slug} - Should display survey form")
    void testTakeSurveyView() throws Exception {
        mockMvc.perform(get("/survey/developer-survey-2026"))
                .andExpect(status().isOk())
                .andExpect(view().name("public/take-survey"))
                .andExpect(model().attributeExists("survey"))
                .andExpect(content().string(containsString("Developer Ecosystem")));
    }

    @Test
    @DisplayName("GET /api/surveys/{id}/stats - Should return JSON analytics for Chart.js")
    void testApiSurveyStatsJson() throws Exception {
        Survey survey = surveyRepository.findBySlug("developer-survey-2026").orElseThrow();

        mockMvc.perform(get("/api/surveys/" + survey.getId() + "/stats")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title", is("2026 Developer Ecosystem & AI Survey")))
                .andExpect(jsonPath("$.totalSubmissions", greaterThanOrEqualTo(10)))
                .andExpect(jsonPath("$.questionStats", hasSize(greaterThanOrEqualTo(3))));
    }

    @Test
    @DisplayName("GET /admin - Should display admin console with KPI cards")
    void testAdminDashboard() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("totalSurveys"))
                .andExpect(model().attributeExists("totalResponses"));
    }

    @Test
    @DisplayName("POST /admin/surveys/{id}/toggle - Should toggle active state")
    void testToggleSurveyState() throws Exception {
        Survey survey = surveyRepository.findBySlug("campus-facilities-2026").orElseThrow();

        mockMvc.perform(post("/admin/surveys/" + survey.getId() + "/toggle"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/surveys"));
    }
}
