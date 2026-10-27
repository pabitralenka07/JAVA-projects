package com.portfolio.medical.controller;

import com.portfolio.medical.model.Patient;
import com.portfolio.medical.model.Role;
import com.portfolio.medical.model.User;
import com.portfolio.medical.service.DoctorService;
import com.portfolio.medical.service.PatientService;
import com.portfolio.medical.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class AuthController {

    private final UserService userService;
    private final PatientService patientService;
    private final DoctorService doctorService;

    public AuthController(UserService userService,
                          PatientService patientService,
                          DoctorService doctorService) {
        this.userService = userService;
        this.patientService = patientService;
        this.doctorService = doctorService;
    }

    @GetMapping("/")
    public String index(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            // Already logged in - redirect to appropriate dashboard
            for (var auth : authentication.getAuthorities()) {
                if ("ROLE_ADMIN".equals(auth.getAuthority())) return "redirect:/admin/dashboard";
                if ("ROLE_DOCTOR".equals(auth.getAuthority())) return "redirect:/doctor/dashboard";
                if ("ROLE_PATIENT".equals(auth.getAuthority())) return "redirect:/patient/dashboard";
            }
        }
        model.addAttribute("specializations", doctorService.getAllSpecializations());
        model.addAttribute("doctors", doctorService.findAll());
        return "index";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "registered", required = false) String registered,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully.");
        }
        if (registered != null) {
            model.addAttribute("successMessage", "Registration successful! Please log in with your credentials.");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String handleRegister(@RequestParam("username") String username,
                                 @RequestParam("password") String password,
                                 @RequestParam("fullName") String fullName,
                                 @RequestParam("email") String email,
                                 @RequestParam(value = "phone", required = false) String phone,
                                 @RequestParam(value = "dateOfBirth", required = false) String dobStr,
                                 @RequestParam(value = "gender", required = false) String gender,
                                 @RequestParam(value = "bloodGroup", required = false) String bloodGroup,
                                 @RequestParam(value = "emergencyContact", required = false) String emergencyContact,
                                 @RequestParam(value = "address", required = false) String address,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = userService.registerUser(username, password, fullName, email, phone, Role.ROLE_PATIENT);
            LocalDate dob = (dobStr != null && !dobStr.isBlank()) ? LocalDate.parse(dobStr) : null;
            Patient patient = new Patient(user, dob, gender, bloodGroup, emergencyContact, address);
            patientService.save(patient);
            redirectAttributes.addFlashAttribute("successMessage", "Account created successfully! Please log in.");
            return "redirect:/login";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Registration failed: " + ex.getMessage());
            return "redirect:/register";
        }
    }
}
