package com.portfolio.survey.repository;

import com.portfolio.survey.model.ResponseAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResponseAnswerRepository extends JpaRepository<ResponseAnswer, Long> {

    List<ResponseAnswer> findByQuestionId(Long questionId);

    @Query("SELECT a FROM ResponseAnswer a WHERE a.question.id = :questionId AND a.textAnswer IS NOT NULL")
    List<ResponseAnswer> findTextAnswersByQuestionId(@Param("questionId") Long questionId);
}
