package com.portfolio.survey.service;

import com.portfolio.survey.dto.QuestionFormDto;
import com.portfolio.survey.dto.SurveyCreateRequest;
import com.portfolio.survey.dto.SurveySubmissionDto;
import com.portfolio.survey.model.*;
import com.portfolio.survey.repository.QuestionRepository;
import com.portfolio.survey.repository.SurveyRepository;
import com.portfolio.survey.repository.SurveyResponseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SurveyServiceTest {

    @Mock
    private SurveyRepository surveyRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private SurveyResponseRepository responseRepository;

    @InjectMocks
    private SurveyService surveyService;

    private Survey sampleSurvey;
    private Question singleChoiceQuestion;
    private Question textQuestion;

    @BeforeEach
    void setUp() {
        sampleSurvey = new Survey("AI in 2026", "Exploring AI usage", "ai-in-2026");
        sampleSurvey.setId(1L);

        singleChoiceQuestion = new Question("Do you use AI daily?", QuestionType.SINGLE_CHOICE, true, 1);
        singleChoiceQuestion.setId(10L);
        singleChoiceQuestion.addOption(new QuestionOption("Yes", 1));
        singleChoiceQuestion.addOption(new QuestionOption("No", 2));

        textQuestion = new Question("Any suggestions?", QuestionType.TEXT, false, 2);
        textQuestion.setId(20L);

        sampleSurvey.addQuestion(singleChoiceQuestion);
        sampleSurvey.addQuestion(textQuestion);
    }

    @Test
    @DisplayName("Should create survey and automatically generate slug if not provided")
    void testCreateSurveySlugGeneration() {
        SurveyCreateRequest request = new SurveyCreateRequest("My Awesome New Survey!", "Description", "");
        request.setQuestions(List.of(
                new QuestionFormDto("Select language", QuestionType.SINGLE_CHOICE, true, List.of("Java", "Go"))
        ));

        when(surveyRepository.existsBySlug("my-awesome-new-survey")).thenReturn(false);
        when(surveyRepository.save(any(Survey.class))).thenAnswer(invocation -> {
            Survey s = invocation.getArgument(0);
            s.setId(100L);
            return s;
        });

        Survey created = surveyService.createSurvey(request);

        assertThat(created).isNotNull();
        assertThat(created.getTitle()).isEqualTo("My Awesome New Survey!");
        assertThat(created.getSlug()).isEqualTo("my-awesome-new-survey");
        assertThat(created.getQuestions()).hasSize(1);
        assertThat(created.getQuestionList().get(0).getOptions()).hasSize(2);
    }

    @Test
    @DisplayName("Should submit valid response successfully")
    void testSubmitValidResponse() {
        when(surveyRepository.findById(1L)).thenReturn(Optional.of(sampleSurvey));
        when(questionRepository.findBySurveyIdOrderBySortOrderAsc(1L))
                .thenReturn(List.of(singleChoiceQuestion, textQuestion));
        when(responseRepository.save(any(SurveyResponse.class))).thenAnswer(i -> i.getArgument(0));

        SurveySubmissionDto submission = new SurveySubmissionDto(1L);
        submission.getAnswers().put(10L, "1"); // Selected option ID 1
        submission.getAnswers().put(20L, "Keep up the great work!");

        SurveyResponse response = surveyService.submitResponse(1L, submission);

        assertThat(response).isNotNull();
        assertThat(response.getAnswers()).hasSize(2);
        verify(responseRepository, times(1)).save(any(SurveyResponse.class));
    }

    @Test
    @DisplayName("Should throw exception when submitting response to closed survey")
    void testSubmitResponseToClosedSurveyThrowsException() {
        sampleSurvey.setActive(false);
        when(surveyRepository.findById(1L)).thenReturn(Optional.of(sampleSurvey));

        SurveySubmissionDto submission = new SurveySubmissionDto(1L);

        assertThatThrownBy(() -> surveyService.submitResponse(1L, submission))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("closed");
    }

    @Test
    @DisplayName("Should throw exception when required question is unanswered")
    void testRequiredQuestionThrowsException() {
        when(surveyRepository.findById(1L)).thenReturn(Optional.of(sampleSurvey));
        when(questionRepository.findBySurveyIdOrderBySortOrderAsc(1L))
                .thenReturn(List.of(singleChoiceQuestion, textQuestion));

        SurveySubmissionDto submission = new SurveySubmissionDto(1L);
        // Leave required single choice question 10 empty!

        assertThatThrownBy(() -> surveyService.submitResponse(1L, submission))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("is required");
    }

    @Test
    @DisplayName("Should toggle survey status from active to inactive and back")
    void testToggleSurveyStatus() {
        when(surveyRepository.findById(1L)).thenReturn(Optional.of(sampleSurvey));
        when(surveyRepository.save(any(Survey.class))).thenAnswer(i -> i.getArgument(0));

        assertThat(sampleSurvey.isActive()).isTrue();

        Survey updated = surveyService.toggleStatus(1L);
        assertThat(updated.isActive()).isFalse();

        Survey updatedAgain = surveyService.toggleStatus(1L);
        assertThat(updatedAgain.isActive()).isTrue();
    }
}
