package com.portfolio.medical.service;

import com.portfolio.medical.exception.ResourceNotFoundException;
import com.portfolio.medical.model.*;
import com.portfolio.medical.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository,
                                PrescriptionRepository prescriptionRepository,
                                PatientRepository patientRepository,
                                DoctorRepository doctorRepository,
                                AppointmentRepository appointmentRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public MedicalRecord createRecord(Long patientId, Long doctorId, LocalDate recordDate,
                                      String diagnosis, String symptoms, String treatmentPlan, String notes) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));

        MedicalRecord record = new MedicalRecord(
                patient, doctor, recordDate != null ? recordDate : LocalDate.now(),
                diagnosis, symptoms, treatmentPlan, notes
        );
        return medicalRecordRepository.save(record);
    }

    public Prescription issuePrescription(Long appointmentId, Long patientId, Long doctorId,
                                          String medications, String dosageInstructions, String notes, LocalDate issueDate) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));

        Appointment appointment = null;
        if (appointmentId != null) {
            appointment = appointmentRepository.findById(appointmentId).orElse(null);
        }

        Prescription prescription = new Prescription(
                appointment, patient, doctor,
                medications, dosageInstructions, notes,
                issueDate != null ? issueDate : LocalDate.now()
        );
        return prescriptionRepository.save(prescription);
    }

    @Transactional(readOnly = true)
    public List<MedicalRecord> getPatientHistory(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));
        return medicalRecordRepository.findByPatientOrderByRecordDateDesc(patient);
    }

    @Transactional(readOnly = true)
    public List<Prescription> getPatientPrescriptions(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));
        return prescriptionRepository.findByPatientOrderByIssueDateDesc(patient);
    }

    @Transactional(readOnly = true)
    public List<Prescription> getDoctorPrescriptions(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));
        return prescriptionRepository.findByDoctorOrderByIssueDateDesc(doctor);
    }

    @Transactional(readOnly = true)
    public List<MedicalRecord> findAllRecords() {
        return medicalRecordRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Prescription> findAllPrescriptions() {
        return prescriptionRepository.findAll();
    }
}
