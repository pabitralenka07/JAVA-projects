package com.portfolio.medical;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MedicalSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Public routes (/ and /login) should be accessible anonymously")
    void testPublicRoutesAccessible() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("HealthPulse Clinic")));

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Portal Sign In")));
    }

    @Test
    @DisplayName("Protected dashboards should redirect unauthenticated requests to /login")
    void testUnauthenticatedRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/doctor/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/patient/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "pat_john", roles = {"PATIENT"})
    @DisplayName("Patient should be forbidden (403) from accessing /admin/dashboard")
    void testPatientCannotAccessAdminDashboard() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "dr_house", roles = {"DOCTOR"})
    @DisplayName("Doctor should be forbidden (403) from accessing /admin/dashboard")
    void testDoctorCannotAccessAdminDashboard() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Admin user should access /admin/dashboard with 200 OK")
    void testAdminCanAccessAdminDashboard() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Executive Command Center")));
    }

    @Test
    @WithMockUser(username = "dr_house", roles = {"DOCTOR"})
    @DisplayName("Doctor user should access /doctor/dashboard with 200 OK")
    void testDoctorCanAccessDoctorDashboard() throws Exception {
        mockMvc.perform(get("/doctor/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Dr. Gregory House")));
    }

    @Test
    @WithMockUser(username = "pat_john", roles = {"PATIENT"})
    @DisplayName("Patient user should access /patient/dashboard with 200 OK")
    void testPatientCanAccessPatientDashboard() throws Exception {
        mockMvc.perform(get("/patient/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello, John Doe")));
    }
}
