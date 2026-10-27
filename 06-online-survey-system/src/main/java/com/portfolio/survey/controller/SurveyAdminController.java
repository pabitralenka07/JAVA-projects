package com.portfolio.survey.controller;

import com.portfolio.survey.dto.QuestionFormDto;
import com.portfolio.survey.dto.SurveyAnalyticsDto;
import com.portfolio.survey.dto.SurveyCreateRequest;
import com.portfolio.survey.model.QuestionType;
import com.portfolio.survey.model.Survey;
import com.portfolio.survey.service.SurveyAnalyticsService;
import com.portfolio.survey.service.SurveyService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class SurveyAdminController {

    private final SurveyService surveyService;
    private final SurveyAnalyticsService analyticsService;

    public SurveyAdminController(SurveyService surveyService,
                                 SurveyAnalyticsService analyticsService) {
        this.surveyService = surveyService;
        this.analyticsService = analyticsService;
    }

    @GetMapping({"", "/", "/surveys"})
    public String dashboard(Model model) {
        List<Survey> surveys = surveyService.getAllSurveys();
        long totalSurveys = surveys.size();
        long totalResponses = surveys.stream().mapToLong(Survey::getResponseCount).sum();
        long activeSurveys = surveys.stream().filter(Survey::isActive).count();

        model.addAttribute("surveys", surveys);
        model.addAttribute("totalSurveys", totalSurveys);
        model.addAttribute("totalResponses", totalResponses);
        model.addAttribute("activeSurveys", activeSurveys);
        return "admin/dashboard";
    }

    @GetMapping("/surveys/new")
    public String newSurveyForm(Model model) {
        SurveyCreateRequest request = new SurveyCreateRequest();
        // Pre-populate with 2 default question slots for a smooth creation experience
        List<QuestionFormDto> defaultQuestions = new ArrayList<>();
        defaultQuestions.add(new QuestionFormDto("", QuestionType.SINGLE_CHOICE, true, List.of()));
        defaultQuestions.add(new QuestionFormDto("", QuestionType.TEXT, false, List.of()));
        request.setQuestions(defaultQuestions);

        model.addAttribute("surveyRequest", request);
        model.addAttribute("questionTypes", QuestionType.values());
        return "admin/create-survey";
    }

    @PostMapping("/surveys")
    public String createSurvey(@Valid @ModelAttribute("surveyRequest") SurveyCreateRequest request,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("questionTypes", QuestionType.values());
            return "admin/create-survey";
        }

        try {
            Survey created = surveyService.createSurvey(request);
            redirectAttributes.addFlashAttribute("successMessage", "Survey '" + created.getTitle() + "' created successfully!");
            return "redirect:/admin/surveys/" + created.getId() + "/results";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Failed to create survey: " + ex.getMessage());
            model.addAttribute("questionTypes", QuestionType.values());
            return "admin/create-survey";
        }
    }

    @GetMapping("/surveys/{id}/results")
    public String viewResults(@PathVariable("id") Long id, Model model) {
        Survey survey = surveyService.getSurveyById(id);
        SurveyAnalyticsDto analytics = analyticsService.getAnalyticsForSurvey(id);
        model.addAttribute("survey", survey);
        model.addAttribute("analytics", analytics);
        return "admin/results";
    }

    @PostMapping("/surveys/{id}/toggle")
    public String toggleStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Survey updated = surveyService.toggleStatus(id);
        String state = updated.isActive() ? "activated" : "closed";
        redirectAttributes.addFlashAttribute("successMessage", "Survey has been " + state + ".");
        return "redirect:/admin/surveys";
    }

    @PostMapping("/surveys/{id}/delete")
    public String deleteSurvey(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        surveyService.deleteSurvey(id);
        redirectAttributes.addFlashAttribute("successMessage", "Survey was successfully deleted.");
        return "redirect:/admin/surveys";
    }
}
