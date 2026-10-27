package com.portfolio.resume.controller;

import com.portfolio.resume.model.Resume;
import com.portfolio.resume.service.ResumeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resumes")
public class ResumeApiController {

    private final ResumeService resumeService;

    public ResumeApiController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resume> getResumeJson(@PathVariable("id") Long id) {
        Resume resume = resumeService.getResumeById(id);
        return ResponseEntity.ok(resume);
    }
}
