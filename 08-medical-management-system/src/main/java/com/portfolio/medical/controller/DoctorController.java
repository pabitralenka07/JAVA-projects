package com.portfolio.medical.controller;

import com.portfolio.medical.model.*;
import com.portfolio.medical.service.AppointmentService;
import com.portfolio.medical.service.DoctorService;
import com.portfolio.medical.service.MedicalRecordService;
import com.portfolio.medical.service.PatientService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;

@Controller
@RequestMapping("/doctor")
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorController {

    private final DoctorService doctorService;
    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final MedicalRecordService medicalRecordService;

    public DoctorController(DoctorService doctorService,
                            PatientService patientService,
                            AppointmentService appointmentService,
                            MedicalRecordService medicalRecordService) {
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.appointmentService = appointmentService;
        this.medicalRecordService = medicalRecordService;
    }

    private Doctor getCurrentDoctor(Principal principal) {
        return doctorService.findByUsername(principal.getName());
    }

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        Doctor doctor = getCurrentDoctor(principal);
        var appointments = appointmentService.getAppointmentsForDoctor(doctor.getId());
        var prescriptions = medicalRecordService.getDoctorPrescriptions(doctor.getId());

        model.addAttribute("doctor", doctor);
        model.addAttribute("appointments", appointments);
        model.addAttribute("prescriptions", prescriptions);
        return "doctor/dashboard";
    }

    @PostMapping("/appointments/{id}/status")
    public String updateStatus(@PathVariable("id") Long id,
                               @RequestParam("status") AppointmentStatus status,
                               @RequestParam(value = "notes", required = false) String notes,
                               RedirectAttributes redirectAttributes) {
        try {
            appointmentService.transitionStatus(id, status, notes);
            redirectAttributes.addFlashAttribute("successMessage", "Appointment #" + id + " updated to " + status);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Status update failed: " + ex.getMessage());
        }
        return "redirect:/doctor/dashboard";
    }

    @GetMapping("/prescriptions/new")
    public String newPrescriptionForm(@RequestParam(value = "patientId") Long patientId,
                                      @RequestParam(value = "appointmentId", required = false) Long appointmentId,
                                      Model model) {
        Patient patient = patientService.findById(patientId);
        model.addAttribute("patient", patient);
        model.addAttribute("appointmentId", appointmentId);
        return "doctor/prescription-form";
    }

    @PostMapping("/prescriptions")
    public String savePrescription(@RequestParam("patientId") Long patientId,
                                   @RequestParam(value = "appointmentId", required = false) Long appointmentId,
                                   @RequestParam("medications") String medications,
                                   @RequestParam("dosageInstructions") String dosageInstructions,
                                   @RequestParam(value = "notes", required = false) String notes,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        try {
            Doctor doctor = getCurrentDoctor(principal);
            medicalRecordService.issuePrescription(
                    appointmentId, patientId, doctor.getId(),
                    medications, dosageInstructions, notes, LocalDate.now()
            );
            redirectAttributes.addFlashAttribute("successMessage", "Prescription created and linked to patient successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create prescription: " + ex.getMessage());
        }
        return "redirect:/doctor/dashboard";
    }

    @GetMapping("/records/new")
    public String newMedicalRecordForm(@RequestParam("patientId") Long patientId, Model model) {
        Patient patient = patientService.findById(patientId);
        model.addAttribute("patient", patient);
        return "doctor/record-form";
    }

    @PostMapping("/records")
    public String saveMedicalRecord(@RequestParam("patientId") Long patientId,
                                    @RequestParam("diagnosis") String diagnosis,
                                    @RequestParam(value = "symptoms", required = false) String symptoms,
                                    @RequestParam(value = "treatmentPlan", required = false) String treatmentPlan,
                                    @RequestParam(value = "notes", required = false) String notes,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        try {
            Doctor doctor = getCurrentDoctor(principal);
            medicalRecordService.createRecord(
                    patientId, doctor.getId(), LocalDate.now(),
                    diagnosis, symptoms, treatmentPlan, notes
            );
            redirectAttributes.addFlashAttribute("successMessage", "Medical diagnostic record saved to patient history!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to save record: " + ex.getMessage());
        }
        return "redirect:/doctor/dashboard";
    }
}
