package com.portfolio.medical.controller;

import com.portfolio.medical.model.Doctor;
import com.portfolio.medical.model.Role;
import com.portfolio.medical.model.User;
import com.portfolio.medical.service.AppointmentService;
import com.portfolio.medical.service.DoctorService;
import com.portfolio.medical.service.MedicalRecordService;
import com.portfolio.medical.service.PatientService;
import com.portfolio.medical.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final DoctorService doctorService;
    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final MedicalRecordService medicalRecordService;
    private final UserService userService;

    public AdminController(DoctorService doctorService,
                           PatientService patientService,
                           AppointmentService appointmentService,
                           MedicalRecordService medicalRecordService,
                           UserService userService) {
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.appointmentService = appointmentService;
        this.medicalRecordService = medicalRecordService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        var doctors = doctorService.findAll();
        var patients = patientService.findAll();
        var appointments = appointmentService.findAll();
        var prescriptions = medicalRecordService.findAllPrescriptions();

        model.addAttribute("doctors", doctors);
        model.addAttribute("patients", patients);
        model.addAttribute("appointments", appointments);
        model.addAttribute("doctorCount", doctors.size());
        model.addAttribute("patientCount", patients.size());
        model.addAttribute("appointmentCount", appointments.size());
        model.addAttribute("prescriptionCount", prescriptions.size());

        return "admin/dashboard";
    }

    @GetMapping("/doctors/new")
    public String newDoctorForm() {
        return "admin/doctor-form";
    }

    @PostMapping("/doctors")
    public String createDoctor(@RequestParam("username") String username,
                               @RequestParam("password") String password,
                               @RequestParam("fullName") String fullName,
                               @RequestParam("email") String email,
                               @RequestParam(value = "phone", required = false) String phone,
                               @RequestParam("specialization") String specialization,
                               @RequestParam(value = "consultationFee", defaultValue = "100.00") BigDecimal fee,
                               @RequestParam(value = "roomNumber", required = false) String roomNumber,
                               @RequestParam(value = "availableDays", defaultValue = "Monday,Tuesday,Wednesday,Thursday,Friday") String days,
                               @RequestParam(value = "bio", required = false) String bio,
                               RedirectAttributes redirectAttributes) {
        try {
            User user = userService.registerUser(username, password, fullName, email, phone, Role.ROLE_DOCTOR);
            Doctor doctor = new Doctor(user, specialization, fee, roomNumber, days, bio);
            doctorService.save(doctor);
            redirectAttributes.addFlashAttribute("successMessage", "Doctor " + fullName + " registered successfully!");
            return "redirect:/admin/dashboard";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add doctor: " + ex.getMessage());
            return "redirect:/admin/doctors/new";
        }
    }
}
