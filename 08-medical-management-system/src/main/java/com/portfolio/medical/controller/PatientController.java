package com.portfolio.medical.controller;

import com.portfolio.medical.model.*;
import com.portfolio.medical.service.AppointmentService;
import com.portfolio.medical.service.DoctorService;
import com.portfolio.medical.service.MedicalRecordService;
import com.portfolio.medical.service.PatientService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/patient")
@PreAuthorize("hasRole('PATIENT')")
public class PatientController {

    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final MedicalRecordService medicalRecordService;

    public PatientController(PatientService patientService,
                             DoctorService doctorService,
                             AppointmentService appointmentService,
                             MedicalRecordService medicalRecordService) {
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
        this.medicalRecordService = medicalRecordService;
    }

    private Patient getCurrentPatient(Principal principal) {
        return patientService.findByUsername(principal.getName());
    }

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        Patient patient = getCurrentPatient(principal);
        var appointments = appointmentService.getAppointmentsForPatient(patient.getId());
        var records = medicalRecordService.getPatientHistory(patient.getId());
        var prescriptions = medicalRecordService.getPatientPrescriptions(patient.getId());

        model.addAttribute("patient", patient);
        model.addAttribute("appointments", appointments);
        model.addAttribute("records", records);
        model.addAttribute("prescriptions", prescriptions);
        return "patient/dashboard";
    }

    @GetMapping("/appointments/new")
    public String newAppointmentForm(@RequestParam(value = "specialization", required = false) String specialization,
                                     Principal principal,
                                     Model model) {
        Patient patient = getCurrentPatient(principal);
        var doctors = doctorService.findBySpecialization(specialization);
        var specializations = doctorService.getAllSpecializations();

        model.addAttribute("patient", patient);
        model.addAttribute("doctors", doctors);
        model.addAttribute("specializations", specializations);
        model.addAttribute("selectedSpecialization", specialization);
        return "patient/book-appointment";
    }

    @PostMapping("/appointments")
    public String bookAppointment(@RequestParam("doctorId") Long doctorId,
                                  @RequestParam("appointmentDateTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime appointmentDateTime,
                                  @RequestParam("reason") String reason,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        try {
            Patient patient = getCurrentPatient(principal);
            appointmentService.scheduleAppointment(patient.getId(), doctorId, appointmentDateTime, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Appointment booked successfully! Awaiting clinic confirmation.");
            return "redirect:/patient/dashboard";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Booking failed: " + ex.getMessage());
            return "redirect:/patient/appointments/new";
        }
    }

    @PostMapping("/appointments/{id}/cancel")
    public String cancelAppointment(@PathVariable("id") Long id,
                                    @RequestParam(value = "reason", defaultValue = "Cancelled by patient") String reason,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        try {
            appointmentService.cancelAppointment(id, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Appointment #" + id + " cancelled successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cancellation failed: " + ex.getMessage());
        }
        return "redirect:/patient/dashboard";
    }

    @GetMapping("/prescriptions")
    public String viewPrescriptions(Principal principal, Model model) {
        Patient patient = getCurrentPatient(principal);
        var prescriptions = medicalRecordService.getPatientPrescriptions(patient.getId());
        model.addAttribute("patient", patient);
        model.addAttribute("prescriptions", prescriptions);
        return "patient/prescriptions";
    }

    @GetMapping("/records")
    public String viewMedicalRecords(Principal principal, Model model) {
        Patient patient = getCurrentPatient(principal);
        var records = medicalRecordService.getPatientHistory(patient.getId());
        model.addAttribute("patient", patient);
        model.addAttribute("records", records);
        return "patient/records";
    }
}
