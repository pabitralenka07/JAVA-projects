package com.portfolio.resume;

import com.portfolio.resume.model.Resume;
import com.portfolio.resume.repository.ResumeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ResumeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResumeRepository resumeRepository;

    @Test
    @DisplayName("GET / - Should display portal with seeded resume")
    void testListResumesPortal() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("resumes"))
                .andExpect(content().string(containsString("ResumeSphere")));
    }

    @Test
    @DisplayName("GET /resumes/{id} - Should display live HTML preview of resume")
    void testResumePreviewView() throws Exception {
        Resume resume = resumeRepository.findAll().get(0);

        mockMvc.perform(get("/resumes/" + resume.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("resume/preview"))
                .andExpect(model().attributeExists("resume"))
                .andExpect(content().string(containsString("Alex Mercer")));
    }

    @Test
    @DisplayName("GET /resumes/{id}/pdf - Should stream generated OpenPDF document")
    void testDownloadPdfEndpoint() throws Exception {
        Resume resume = resumeRepository.findAll().get(0);

        mockMvc.perform(get("/resumes/" + resume.getId() + "/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", containsString("_Resume.pdf")));
    }

    @Test
    @DisplayName("GET /api/resumes/{id} - Should return JSON resume data")
    void testApiResumeJson() throws Exception {
        Resume resume = resumeRepository.findAll().get(0);

        mockMvc.perform(get("/api/resumes/" + resume.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.fullName", is("Alex Mercer")));
    }

    @Test
    @DisplayName("GET /resumes/new - Should display create resume form")
    void testNewResumeForm() throws Exception {
        mockMvc.perform(get("/resumes/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("resume/form"))
                .andExpect(model().attributeExists("resumeForm"));
    }
}
