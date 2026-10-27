package com.portfolio.survey.repository;

import com.portfolio.survey.model.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {

    long countBySurveyId(Long surveyId);

    List<SurveyResponse> findBySurveyIdOrderBySubmittedAtDesc(Long surveyId);

    @Query("SELECT r FROM SurveyResponse r LEFT JOIN FETCH r.answers a LEFT JOIN FETCH a.question WHERE r.survey.id = :surveyId")
    List<SurveyResponse> findAllBySurveyIdWithAnswers(@Param("surveyId") Long surveyId);
}
