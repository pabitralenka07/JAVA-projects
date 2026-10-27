package com.portfolio.survey.repository;

import com.portfolio.survey.model.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {

    Optional<Survey> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Survey> findAllByOrderByCreatedAtDesc();

    @Query("SELECT DISTINCT s FROM Survey s LEFT JOIN FETCH s.questions q LEFT JOIN FETCH q.options WHERE s.slug = :slug")
    Optional<Survey> findBySlugWithQuestions(@Param("slug") String slug);

    @Query("SELECT DISTINCT s FROM Survey s LEFT JOIN FETCH s.questions q LEFT JOIN FETCH q.options WHERE s.id = :id")
    Optional<Survey> findByIdWithQuestions(@Param("id") Long id);
}
