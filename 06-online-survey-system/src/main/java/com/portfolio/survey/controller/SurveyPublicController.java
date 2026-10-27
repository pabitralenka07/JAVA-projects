package com.portfolio.survey.controller;

import com.portfolio.survey.dto.SurveySubmissionDto;
import com.portfolio.survey.model.Survey;
import com.portfolio.survey.service.SurveyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

@Controller
public class SurveyPublicController {

    private final SurveyService surveyService;

    public SurveyPublicController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<Survey> surveys = surveyService.getAllSurveys();
        model.addAttribute("surveys", surveys);
        return "index";
    }

    @GetMapping("/surveys")
    public String listSurveys(Model model) {
        List<Survey> surveys = surveyService.getAllSurveys();
        model.addAttribute("surveys", surveys);
        return "index";
    }

    @GetMapping("/survey/{slug}")
    public String takeSurvey(@PathVariable("slug") String slug, Model model) {
        Survey survey = surveyService.getSurveyBySlug(slug);
        model.addAttribute("survey", survey);
        model.addAttribute("submission", new SurveySubmissionDto(survey.getId()));
        return "public/take-survey";
    }

    @PostMapping("/survey/{slug}/submit")
    public String submitSurvey(@PathVariable("slug") String slug,
                               @ModelAttribute SurveySubmissionDto submission,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        try {
            Survey survey = surveyService.getSurveyBySlug(slug);
            submission.setSurveyId(survey.getId());

            // Compute pseudo-anonymous respondent fingerprint
            String clientIp = getClientIp(request);
            String userAgent = request.getHeader("User-Agent");
            submission.setRespondentId(generateRespondentHash(clientIp, userAgent));

            surveyService.submitResponse(survey.getId(), submission);
            redirectAttributes.addFlashAttribute("successMessage", "Thank you! Your feedback has been recorded.");
            return "redirect:/survey/" + slug + "/thank-you";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/survey/" + slug;
        }
    }

    @GetMapping("/survey/{slug}/thank-you")
    public String thankYou(@PathVariable("slug") String slug, Model model) {
        Survey survey = surveyService.getSurveyBySlug(slug);
        model.addAttribute("survey", survey);
        return "public/thank-you";
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
    }

    private String generateRespondentHash(String ip, String userAgent) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest((ip + "|" + userAgent).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 16);
        } catch (Exception e) {
            return "anon-" + System.currentTimeMillis();
        }
    }
}
