package com.portfolio.survey.service;

import com.portfolio.survey.dto.OptionStatsDto;
import com.portfolio.survey.dto.QuestionStatsDto;
import com.portfolio.survey.dto.SurveyAnalyticsDto;
import com.portfolio.survey.model.*;
import com.portfolio.survey.repository.SurveyRepository;
import com.portfolio.survey.repository.SurveyResponseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class SurveyAnalyticsService {

    private final SurveyRepository surveyRepository;
    private final SurveyResponseRepository responseRepository;

    public SurveyAnalyticsService(SurveyRepository surveyRepository,
                                  SurveyResponseRepository responseRepository) {
        this.surveyRepository = surveyRepository;
        this.responseRepository = responseRepository;
    }

    public SurveyAnalyticsDto getAnalyticsForSurvey(Long surveyId) {
        Survey survey = surveyRepository.findByIdWithQuestions(surveyId)
                .orElseThrow(() -> new NoSuchElementException("Survey not found with ID: " + surveyId));

        List<SurveyResponse> responses = responseRepository.findAllBySurveyIdWithAnswers(surveyId);
        long totalSubmissions = responses.size();

        SurveyAnalyticsDto analytics = new SurveyAnalyticsDto(
                survey.getId(),
                survey.getTitle(),
                survey.getSlug(),
                totalSubmissions
        );

        List<QuestionStatsDto> questionStatsList = new ArrayList<>();

        for (Question question : survey.getQuestions()) {
            QuestionStatsDto qStats = new QuestionStatsDto(
                    question.getId(),
                    question.getPrompt(),
                    question.getQuestionType()
            );

            // Filter all answers associated with this question
            List<ResponseAnswer> questionAnswers = responses.stream()
                    .flatMap(r -> r.getAnswers().stream())
                    .filter(a -> a.getQuestion() != null && Objects.equals(a.getQuestion().getId(), question.getId()))
                    .toList();

            qStats.setTotalResponses(questionAnswers.size());

            if (question.getQuestionType() == QuestionType.SINGLE_CHOICE) {
                // Count single choice selections
                Map<Long, Long> countsByOption = new HashMap<>();
                for (ResponseAnswer a : questionAnswers) {
                    if (a.getSelectedOptionId() != null) {
                        countsByOption.merge(a.getSelectedOptionId(), 1L, Long::sum);
                    }
                }

                for (QuestionOption opt : question.getOptions()) {
                    long count = countsByOption.getOrDefault(opt.getId(), 0L);
                    double pct = calculatePercentage(count, totalSubmissions);
                    qStats.getOptionStats().add(new OptionStatsDto(opt.getId(), opt.getOptionText(), count, pct));
                }

            } else if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
                // Count multi-choice selections
                Map<Long, Long> countsByOption = new HashMap<>();
                for (ResponseAnswer a : questionAnswers) {
                    List<Long> selectedIds = a.getMultiOptionIdList();
                    for (Long optId : selectedIds) {
                        countsByOption.merge(optId, 1L, Long::sum);
                    }
                }

                for (QuestionOption opt : question.getOptions()) {
                    long count = countsByOption.getOrDefault(opt.getId(), 0L);
                    double pct = calculatePercentage(count, totalSubmissions);
                    qStats.getOptionStats().add(new OptionStatsDto(opt.getId(), opt.getOptionText(), count, pct));
                }

            } else if (question.getQuestionType() == QuestionType.TEXT) {
                // Gather textual feedback
                List<String> texts = questionAnswers.stream()
                        .map(ResponseAnswer::getTextAnswer)
                        .filter(t -> t != null && !t.isBlank())
                        .toList();
                qStats.setTextResponses(texts);
            }

            questionStatsList.add(qStats);
        }

        analytics.setQuestionStats(questionStatsList);
        return analytics;
    }

    private double calculatePercentage(long count, long total) {
        if (total <= 0) return 0.0;
        double pct = (count * 100.0) / total;
        return BigDecimal.valueOf(pct).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
