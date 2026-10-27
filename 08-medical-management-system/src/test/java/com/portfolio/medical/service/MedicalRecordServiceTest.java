package com.portfolio.medical.service;

import com.portfolio.medical.model.*;
import com.portfolio.medical.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalRecordServiceTest {

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private MedicalRecordService medicalRecordService;

    private Doctor doctor;
    private Patient patient;

    @BeforeEach
    void setUp() {
        User docUser = new User("dr_watson", "pass123", "Dr. John Watson", "watson@clinic.com", "555-1111", Role.ROLE_DOCTOR);
        doctor = new Doctor(docUser, "General Medicine", new BigDecimal("100.00"), "Room 101", "Mon-Fri", "Family Physician");
        doctor.setId(1L);

        User patUser = new User("sherlock", "pass123", "Sherlock Holmes", "sherlock@baker.org", "555-2222", Role.ROLE_PATIENT);
        patient = new Patient(patUser, LocalDate.of(1985, 1, 6), "Male", "AB+", "Mrs. Hudson", "221B Baker St");
        patient.setId(2L);
    }

    @Test
    @DisplayName("Should create medical diagnostic record and retrieve patient history")
    void testCreateMedicalRecordAndHistory() {
        when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        LocalDate today = LocalDate.now();
        MedicalRecord record = new MedicalRecord(
                patient, doctor, today, "Acute Bronchitis",
                "Severe dry cough, mild fever, chest congestion",
                "Rest, hydration, inhaler 2x daily",
                "Follow up in 7 days if symptoms persist"
        );
        record.setId(50L);

        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(record);
        when(medicalRecordRepository.findByPatientOrderByRecordDateDesc(patient)).thenReturn(List.of(record));

        MedicalRecord saved = medicalRecordService.createRecord(
                2L, 1L, today, "Acute Bronchitis",
                "Severe dry cough, mild fever, chest congestion",
                "Rest, hydration, inhaler 2x daily",
                "Follow up in 7 days if symptoms persist"
        );

        assertThat(saved.getId()).isEqualTo(50L);
        assertThat(saved.getDiagnosis()).isEqualTo("Acute Bronchitis");

        List<MedicalRecord> history = medicalRecordService.getPatientHistory(2L);
        assertThat(history).hasSize(1);
        assertThat(history.get(0).getPatient().getUser().getFullName()).isEqualTo("Sherlock Holmes");
    }

    @Test
    @DisplayName("Should issue prescription linked to appointment and patient")
    void testIssuePrescription() {
        when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        Appointment appointment = new Appointment(patient, doctor, LocalDateTime.now(), "Follow-up");
        appointment.setId(30L);
        when(appointmentRepository.findById(30L)).thenReturn(Optional.of(appointment));

        Prescription prescription = new Prescription(
                appointment, patient, doctor,
                "Amoxicillin 500mg, Paracetamol 650mg",
                "Take 1 tablet every 8 hours after meals",
                "Do not skip doses",
                LocalDate.now()
        );
        prescription.setId(70L);

        when(prescriptionRepository.save(any(Prescription.class))).thenReturn(prescription);
        when(prescriptionRepository.findByPatientOrderByIssueDateDesc(patient)).thenReturn(List.of(prescription));

        Prescription result = medicalRecordService.issuePrescription(
                30L, 2L, 1L,
                "Amoxicillin 500mg, Paracetamol 650mg",
                "Take 1 tablet every 8 hours after meals",
                "Do not skip doses",
                LocalDate.now()
        );

        assertThat(result.getId()).isEqualTo(70L);
        assertThat(result.getMedications()).contains("Amoxicillin");
        assertThat(result.getDosageInstructions()).contains("after meals");

        List<Prescription> prescriptions = medicalRecordService.getPatientPrescriptions(2L);
        assertThat(prescriptions).hasSize(1);
        assertThat(prescriptions.get(0).getAppointment()).isNotNull();
        assertThat(prescriptions.get(0).getAppointment().getId()).isEqualTo(30L);
    }
}
