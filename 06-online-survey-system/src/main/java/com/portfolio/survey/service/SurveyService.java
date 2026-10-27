package com.portfolio.survey.service;

import com.portfolio.survey.dto.QuestionFormDto;
import com.portfolio.survey.dto.SurveyCreateRequest;
import com.portfolio.survey.dto.SurveySubmissionDto;
import com.portfolio.survey.model.*;
import com.portfolio.survey.repository.QuestionRepository;
import com.portfolio.survey.repository.SurveyRepository;
import com.portfolio.survey.repository.SurveyResponseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

@Service
@Transactional
public class SurveyService {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    private final SurveyRepository surveyRepository;
    private final QuestionRepository questionRepository;
    private final SurveyResponseRepository responseRepository;

    public SurveyService(SurveyRepository surveyRepository,
                         QuestionRepository questionRepository,
                         SurveyResponseRepository responseRepository) {
        this.surveyRepository = surveyRepository;
        this.questionRepository = questionRepository;
        this.responseRepository = responseRepository;
    }

    @Transactional(readOnly = true)
    public List<Survey> getAllSurveys() {
        return surveyRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Survey getSurveyById(Long id) {
        return surveyRepository.findByIdWithQuestions(id)
                .orElseThrow(() -> new NoSuchElementException("Survey not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public Survey getSurveyBySlug(String slug) {
        return surveyRepository.findBySlugWithQuestions(slug)
                .orElseThrow(() -> new NoSuchElementException("Survey not found with slug: " + slug));
    }

    public Survey createSurvey(SurveyCreateRequest request) {
        String slug = request.getSlug();
        if (slug == null || slug.isBlank()) {
            slug = generateUniqueSlug(request.getTitle());
        } else {
            slug = slugify(slug);
            if (surveyRepository.existsBySlug(slug)) {
                slug = slug + "-" + UUID.randomUUID().toString().substring(0, 6);
            }
        }

        Survey survey = new Survey(request.getTitle(), request.getDescription(), slug);

        if (request.getQuestions() != null) {
            int order = 1;
            for (QuestionFormDto qDto : request.getQuestions()) {
                if (qDto.getPrompt() == null || qDto.getPrompt().isBlank()) {
                    continue;
                }
                Question question = new Question(
                        qDto.getPrompt().trim(),
                        qDto.getQuestionType(),
                        qDto.isRequired(),
                        order++
                );

                if (qDto.getQuestionType() != QuestionType.TEXT) {
                    List<String> options = qDto.resolveOptions();
                    int optOrder = 1;
                    for (String optText : options) {
                        if (!optText.isBlank()) {
                            question.addOption(new QuestionOption(optText.trim(), optOrder++));
                        }
                    }
                }
                survey.addQuestion(question);
            }
        }

        return surveyRepository.save(survey);
    }

    public SurveyResponse submitResponse(Long surveyId, SurveySubmissionDto submission) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new NoSuchElementException("Survey not found with ID: " + surveyId));

        if (!survey.isActive()) {
            throw new IllegalStateException("Survey is currently closed to new responses.");
        }

        String respondent = submission.getRespondentId();
        if (respondent == null || respondent.isBlank()) {
            respondent = UUID.randomUUID().toString();
        }

        SurveyResponse response = new SurveyResponse(survey, respondent);

        Map<Long, String> singleAnswers = submission.getAnswers() != null ? submission.getAnswers() : Collections.emptyMap();
        Map<Long, List<Long>> multiAnswers = submission.getMultiAnswers() != null ? submission.getMultiAnswers() : Collections.emptyMap();

        List<Question> questions = questionRepository.findBySurveyIdOrderBySortOrderAsc(surveyId);

        for (Question q : questions) {
            if (q.getQuestionType() == QuestionType.TEXT) {
                String text = singleAnswers.get(q.getId());
                if (text != null && !text.isBlank()) {
                    response.addAnswer(new ResponseAnswer(q, text.trim()));
                } else if (q.isRequired()) {
                    throw new IllegalArgumentException("Question '" + q.getPrompt() + "' is required.");
                }
            } else if (q.getQuestionType() == QuestionType.SINGLE_CHOICE) {
                String optionIdStr = singleAnswers.get(q.getId());
                if (optionIdStr != null && !optionIdStr.isBlank()) {
                    Long optionId = Long.parseLong(optionIdStr.trim());
                    response.addAnswer(new ResponseAnswer(q, optionId));
                } else if (q.isRequired()) {
                    throw new IllegalArgumentException("Question '" + q.getPrompt() + "' is required.");
                }
            } else if (q.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
                List<Long> selected = multiAnswers.get(q.getId());
                if (selected != null && !selected.isEmpty()) {
                    response.addAnswer(new ResponseAnswer(q, selected));
                } else if (q.isRequired()) {
                    throw new IllegalArgumentException("Question '" + q.getPrompt() + "' requires at least one selection.");
                }
            }
        }

        return responseRepository.save(response);
    }

    public void deleteSurvey(Long id) {
        if (!surveyRepository.existsById(id)) {
            throw new NoSuchElementException("Survey not found with ID: " + id);
        }
        surveyRepository.deleteById(id);
    }

    public Survey toggleStatus(Long id) {
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Survey not found with ID: " + id));
        survey.setActive(!survey.isActive());
        return surveyRepository.save(survey);
    }

    public String generateUniqueSlug(String title) {
        String baseSlug = slugify(title);
        if (baseSlug.isBlank()) {
            baseSlug = "survey-" + System.currentTimeMillis();
        }
        String candidate = baseSlug;
        int counter = 1;
        while (surveyRepository.existsBySlug(candidate)) {
            candidate = baseSlug + "-" + counter++;
        }
        return candidate;
    }

    public static String slugify(String input) {
        if (input == null) return "";
        String nowhitespace = WHITESPACE.matcher(input.trim()).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NON_LATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH).replaceAll("-+", "-").replaceAll("^-|-$", "");
    }
}
