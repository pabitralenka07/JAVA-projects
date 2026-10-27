package com.portfolio.resume.repository;

import com.portfolio.resume.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    List<Resume> findAllByOrderByUpdatedAtDesc();

    @Query("SELECT DISTINCT r FROM Resume r " +
           "LEFT JOIN FETCH r.educations " +
           "LEFT JOIN FETCH r.experiences " +
           "LEFT JOIN FETCH r.projects " +
           "LEFT JOIN FETCH r.skillCategories " +
           "LEFT JOIN FETCH r.certifications " +
           "WHERE r.id = :id")
    Optional<Resume> findByIdWithDetails(@Param("id") Long id);
}
