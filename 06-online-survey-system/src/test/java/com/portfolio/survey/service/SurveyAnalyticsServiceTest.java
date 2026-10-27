package com.portfolio.survey.service;

import com.portfolio.survey.dto.OptionStatsDto;
import com.portfolio.survey.dto.QuestionStatsDto;
import com.portfolio.survey.dto.SurveyAnalyticsDto;
import com.portfolio.survey.model.*;
import com.portfolio.survey.repository.SurveyRepository;
import com.portfolio.survey.repository.SurveyResponseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SurveyAnalyticsServiceTest {

    @Mock
    private SurveyRepository surveyRepository;

    @Mock
    private SurveyResponseRepository responseRepository;

    @InjectMocks
    private SurveyAnalyticsService analyticsService;

    @Test
    @DisplayName("Should aggregate single-choice and multiple-choice counts with correct percentages")
    void testAnalyticsAggregationsAndPercentages() {
        Survey survey = new Survey("Tech Stack", "Tech Stack Analysis", "tech-stack");
        survey.setId(1L);

        // Single Choice Question
        Question q1 = new Question("Preferred Language", QuestionType.SINGLE_CHOICE, true, 1);
        q1.setId(10L);
        QuestionOption optJava = new QuestionOption("Java", 1);
        optJava.setId(101L);
        QuestionOption optPython = new QuestionOption("Python", 2);
        optPython.setId(102L);
        q1.addOption(optJava);
        q1.addOption(optPython);

        // Multiple Choice Question
        Question q2 = new Question("Cloud Providers", QuestionType.MULTIPLE_CHOICE, false, 2);
        q2.setId(20L);
        QuestionOption optAws = new QuestionOption("AWS", 1);
        optAws.setId(201L);
        QuestionOption optGcp = new QuestionOption("GCP", 2);
        optGcp.setId(202L);
        q2.addOption(optAws);
        q2.addOption(optGcp);

        // Text Question
        Question q3 = new Question("Comments", QuestionType.TEXT, false, 3);
        q3.setId(30L);

        survey.addQuestion(q1);
        survey.addQuestion(q2);
        survey.addQuestion(q3);

        // Mock 4 responses:
        // Respondent 1: Java (101), AWS & GCP (201, 202), "Great survey!"
        SurveyResponse r1 = new SurveyResponse(survey, "user-1");
        r1.addAnswer(new ResponseAnswer(q1, 101L));
        r1.addAnswer(new ResponseAnswer(q2, List.of(201L, 202L)));
        r1.addAnswer(new ResponseAnswer(q3, "Great survey!"));

        // Respondent 2: Java (101), AWS (201)
        SurveyResponse r2 = new SurveyResponse(survey, "user-2");
        r2.addAnswer(new ResponseAnswer(q1, 101L));
        r2.addAnswer(new ResponseAnswer(q2, List.of(201L)));

        // Respondent 3: Java (101), GCP (202)
        SurveyResponse r3 = new SurveyResponse(survey, "user-3");
        r3.addAnswer(new ResponseAnswer(q1, 101L));
        r3.addAnswer(new ResponseAnswer(q2, List.of(202L)));

        // Respondent 4: Python (102), AWS (201), "More options please"
        SurveyResponse r4 = new SurveyResponse(survey, "user-4");
        r4.addAnswer(new ResponseAnswer(q1, 102L));
        r4.addAnswer(new ResponseAnswer(q2, List.of(201L)));
        r4.addAnswer(new ResponseAnswer(q3, "More options please"));

        when(surveyRepository.findByIdWithQuestions(1L)).thenReturn(Optional.of(survey));
        when(responseRepository.findAllBySurveyIdWithAnswers(1L)).thenReturn(List.of(r1, r2, r3, r4));

        SurveyAnalyticsDto analytics = analyticsService.getAnalyticsForSurvey(1L);

        assertThat(analytics).isNotNull();
        assertThat(analytics.getTotalSubmissions()).isEqualTo(4);
        assertThat(analytics.getQuestionStats()).hasSize(3);

        // Verify Question 1 (Single choice: 3 Java = 75.0%, 1 Python = 25.0%)
        QuestionStatsDto q1Stats = analytics.getQuestionStats().get(0);
        assertThat(q1Stats.getPrompt()).isEqualTo("Preferred Language");
        OptionStatsDto javaStat = q1Stats.getOptionStats().stream().filter(o -> o.getOptionId().equals(101L)).findFirst().orElseThrow();
        OptionStatsDto pythonStat = q1Stats.getOptionStats().stream().filter(o -> o.getOptionId().equals(102L)).findFirst().orElseThrow();
        assertThat(javaStat.getCount()).isEqualTo(3);
        assertThat(javaStat.getPercentage()).isEqualTo(75.0);
        assertThat(pythonStat.getCount()).isEqualTo(1);
        assertThat(pythonStat.getPercentage()).isEqualTo(25.0);

        // Verify Question 2 (Multi choice: AWS = 3/4 = 75.0%, GCP = 2/4 = 50.0%)
        QuestionStatsDto q2Stats = analytics.getQuestionStats().get(1);
        OptionStatsDto awsStat = q2Stats.getOptionStats().stream().filter(o -> o.getOptionId().equals(201L)).findFirst().orElseThrow();
        OptionStatsDto gcpStat = q2Stats.getOptionStats().stream().filter(o -> o.getOptionId().equals(202L)).findFirst().orElseThrow();
        assertThat(awsStat.getCount()).isEqualTo(3);
        assertThat(awsStat.getPercentage()).isEqualTo(75.0);
        assertThat(gcpStat.getCount()).isEqualTo(2);
        assertThat(gcpStat.getPercentage()).isEqualTo(50.0);

        // Verify Question 3 (Text responses)
        QuestionStatsDto q3Stats = analytics.getQuestionStats().get(2);
        assertThat(q3Stats.getTextResponses()).containsExactlyInAnyOrder("Great survey!", "More options please");
    }
}
