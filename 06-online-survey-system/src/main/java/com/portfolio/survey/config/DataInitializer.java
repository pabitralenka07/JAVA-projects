package com.portfolio.survey.config;

import com.portfolio.survey.dto.QuestionFormDto;
import com.portfolio.survey.dto.SurveyCreateRequest;
import com.portfolio.survey.dto.SurveySubmissionDto;
import com.portfolio.survey.model.Question;
import com.portfolio.survey.model.QuestionOption;
import com.portfolio.survey.model.QuestionType;
import com.portfolio.survey.model.Survey;
import com.portfolio.survey.service.SurveyService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    private final SurveyService surveyService;

    public DataInitializer(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @Override
    public void run(String... args) {
        if (!surveyService.getAllSurveys().isEmpty()) {
            return;
        }

        // 1. Developer Ecosystem Survey
        SurveyCreateRequest devSurvey = new SurveyCreateRequest(
                "2026 Developer Ecosystem & AI Survey",
                "A community pulse on programming language adoption, preferred developer tooling, and AI workflow integration.",
                "developer-survey-2026"
        );

        List<QuestionFormDto> questions = new ArrayList<>();
        questions.add(new QuestionFormDto(
                "Which programming language do you primarily use for backend development?",
                QuestionType.SINGLE_CHOICE,
                true,
                List.of("Java", "Python", "Go", "TypeScript / Node.js", "Kotlin")
        ));
        questions.add(new QuestionFormDto(
                "Which development tools and platforms do you use weekly?",
                QuestionType.MULTIPLE_CHOICE,
                true,
                List.of("Docker & Containers", "IntelliJ IDEA", "VS Code", "Postman / Bruno", "Kubernetes")
        ));
        questions.add(new QuestionFormDto(
                "How frequently do you utilize AI pair programmers in your daily workflow?",
                QuestionType.SINGLE_CHOICE,
                true,
                List.of("Multiple times daily", "Occasionally for boilerplate", "Exploring / Experimenting", "Never")
        ));
        questions.add(new QuestionFormDto(
                "What architectural pattern or Java feature are you most excited about this year?",
                QuestionType.TEXT,
                false,
                List.of()
        ));
        devSurvey.setQuestions(questions);

        Survey createdDev = surveyService.createSurvey(devSurvey);

        // Pre-populate realistic submissions for Dev Survey
        List<Question> devQuestions = createdDev.getQuestionList();
        Question qLang = devQuestions.get(0);
        Question qTools = devQuestions.get(1);
        Question qAi = devQuestions.get(2);
        Question qFeedback = devQuestions.get(3);

        seedDevResponses(createdDev.getId(), qLang, qTools, qAi, qFeedback);

        // 2. Campus Facilities & Learning Tech Survey
        SurveyCreateRequest campusSurvey = new SurveyCreateRequest(
                "Campus Tech Infrastructure & Lab Experience",
                "Evaluate department computer labs, high-speed Wi-Fi reliability, and cloud learning resources.",
                "campus-facilities-2026"
        );

        List<QuestionFormDto> campusQuestions = new ArrayList<>();
        campusQuestions.add(new QuestionFormDto(
                "How would you rate the speed and stability of the campus Wi-Fi network?",
                QuestionType.SINGLE_CHOICE,
                true,
                List.of("Excellent (5/5)", "Good (4/5)", "Average (3/5)", "Unstable / Needs Upgrade")
        ));
        campusQuestions.add(new QuestionFormDto(
                "Which lab resources do you utilize the most?",
                QuestionType.MULTIPLE_CHOICE,
                true,
                List.of("GPU AI Clusters", "Linux Workstations", "Quiet Research Pods", "Hardware Prototyping Lab")
        ));
        campusQuestions.add(new QuestionFormDto(
                "Any specific suggestions or software tool requests for the engineering labs?",
                QuestionType.TEXT,
                false,
                List.of()
        ));
        campusSurvey.setQuestions(campusQuestions);

        Survey createdCampus = surveyService.createSurvey(campusSurvey);
        List<Question> campQs = createdCampus.getQuestionList();
        seedCampusResponses(createdCampus.getId(), campQs.get(0), campQs.get(1), campQs.get(2));
    }

    private void seedDevResponses(Long surveyId, Question qLang, Question qTools, Question qAi, Question qFeedback) {
        List<QuestionOption> langOpts = qLang.getOptionList();
        List<QuestionOption> toolOpts = qTools.getOptionList();
        List<QuestionOption> aiOpts = qAi.getOptionList();

        String[] quotes = {
                "Virtual threads (Project Loom) made our concurrency code so much easier to read and maintain.",
                "Spring Boot 3 native compilation with GraalVM for super fast startup times.",
                "Pattern matching for switch and records drastically cut down ceremony in domain models.",
                "Looking forward to flexible constructor bodies and value objects in upcoming LTS releases.",
                "Cleaner dependency injection and standard JSON handling without extra boilerplate.",
                "Microservices orchestration with Spring Cloud and Docker Compose integration."
        };

        // 12 simulated submissions with varied distributions
        int[][] selections = {
                {0, 0, 0}, {0, 1, 0}, {0, 0, 1}, {1, 2, 0}, {0, 1, 0},
                {2, 0, 1}, {0, 3, 0}, {3, 1, 2}, {4, 0, 0}, {0, 1, 1},
                {1, 2, 0}, {0, 0, 3}
        };

        for (int i = 0; i < selections.length; i++) {
            SurveySubmissionDto sub = new SurveySubmissionDto(surveyId);
            sub.setRespondentId("dev-seed-" + (i + 1));

            // Q1: Language
            int langIdx = selections[i][0] % langOpts.size();
            sub.getAnswers().put(qLang.getId(), String.valueOf(langOpts.get(langIdx).getId()));

            // Q2: Tools (multiple)
            List<Long> toolIds = new ArrayList<>();
            toolIds.add(toolOpts.get(0).getId()); // Docker
            if (i % 2 == 0) toolIds.add(toolOpts.get(1).getId()); // IntelliJ
            if (i % 3 == 0) toolIds.add(toolOpts.get(2).getId()); // VS Code
            if (i % 4 == 0) toolIds.add(toolOpts.get(3).getId()); // Postman
            sub.getMultiAnswers().put(qTools.getId(), toolIds);

            // Q3: AI
            int aiIdx = selections[i][1] % aiOpts.size();
            sub.getAnswers().put(qAi.getId(), String.valueOf(aiOpts.get(aiIdx).getId()));

            // Q4: Feedback
            if (i < quotes.length) {
                sub.getAnswers().put(qFeedback.getId(), quotes[i]);
            }

            surveyService.submitResponse(surveyId, sub);
        }
    }

    private void seedCampusResponses(Long surveyId, Question qWifi, Question qLabs, Question qSuggestions) {
        List<QuestionOption> wifiOpts = qWifi.getOptionList();
        List<QuestionOption> labOpts = qLabs.getOptionList();

        String[] feedback = {
                "More GPU compute time slots for final-year ML capstone projects would be amazing.",
                "Wi-Fi in the central library basement has slight packet loss during peak hours.",
                "Love the 24/7 Linux workstation access during hackathons!",
                "Please add licenses for JetBrains All Products pack for students."
        };

        for (int i = 0; i < 8; i++) {
            SurveySubmissionDto sub = new SurveySubmissionDto(surveyId);
            sub.setRespondentId("campus-seed-" + (i + 1));

            int wifiIdx = (i % 2 == 0) ? 0 : (i % 3 == 0 ? 1 : 2);
            sub.getAnswers().put(qWifi.getId(), String.valueOf(wifiOpts.get(wifiIdx).getId()));

            List<Long> selectedLabIds = new ArrayList<>();
            selectedLabIds.add(labOpts.get(i % labOpts.size()).getId());
            if (i % 2 == 0) selectedLabIds.add(labOpts.get((i + 1) % labOpts.size()).getId());
            sub.getMultiAnswers().put(qLabs.getId(), selectedLabIds);

            if (i < feedback.length) {
                sub.getAnswers().put(qSuggestions.getId(), feedback[i]);
            }

            surveyService.submitResponse(surveyId, sub);
        }
    }
}
