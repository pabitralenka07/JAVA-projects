package com.portfolio.medical.service;

import com.portfolio.medical.exception.AppointmentConflictException;
import com.portfolio.medical.exception.InvalidStatusTransitionException;
import com.portfolio.medical.exception.ResourceNotFoundException;
import com.portfolio.medical.model.Appointment;
import com.portfolio.medical.model.AppointmentStatus;
import com.portfolio.medical.model.Doctor;
import com.portfolio.medical.model.Patient;
import com.portfolio.medical.repository.AppointmentRepository;
import com.portfolio.medical.repository.DoctorRepository;
import com.portfolio.medical.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              DoctorRepository doctorRepository,
                              PatientRepository patientRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    public Appointment scheduleAppointment(Long patientId, Long doctorId, LocalDateTime dateTime, String reason) {
        if (dateTime == null) {
            throw new IllegalArgumentException("Appointment date and time must not be null");
        }

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));

        // Check for double booking (any non-cancelled appointment at this exact slot for the doctor)
        boolean hasConflict = appointmentRepository.existsByDoctorAndAppointmentDateTimeAndStatusNot(
                doctor, dateTime, AppointmentStatus.CANCELLED
        );

        if (hasConflict) {
            String docName = doctor.getUser() != null ? doctor.getUser().getFullName() : "Doctor #" + doctor.getId();
            throw new AppointmentConflictException("Dr. " + docName + " is already booked at " + dateTime);
        }

        Appointment appointment = new Appointment(patient, doctor, dateTime, reason);
        return appointmentRepository.save(appointment);
    }

    public Appointment transitionStatus(Long appointmentId, AppointmentStatus newStatus, String notes) {
        Appointment appointment = findById(appointmentId);

        if (!appointment.canTransitionTo(newStatus)) {
            throw new InvalidStatusTransitionException(
                    "Cannot transition appointment #" + appointmentId + " from " + appointment.getStatus() + " to " + newStatus
            );
        }

        appointment.setStatus(newStatus);
        if (notes != null && !notes.isBlank()) {
            if (appointment.getNotes() == null || appointment.getNotes().isBlank()) {
                appointment.setNotes(notes);
            } else {
                appointment.setNotes(appointment.getNotes() + "\n" + notes);
            }
        }
        return appointmentRepository.save(appointment);
    }

    public Appointment confirmAppointment(Long appointmentId) {
        return transitionStatus(appointmentId, AppointmentStatus.CONFIRMED, "Appointment confirmed by staff/doctor");
    }

    public Appointment completeAppointment(Long appointmentId, String consultationSummary) {
        return transitionStatus(appointmentId, AppointmentStatus.COMPLETED, consultationSummary);
    }

    public Appointment cancelAppointment(Long appointmentId, String cancelReason) {
        return transitionStatus(appointmentId, AppointmentStatus.CANCELLED, "Cancellation reason: " + cancelReason);
    }

    @Transactional(readOnly = true)
    public Appointment findById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsForDoctor(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));
        return appointmentRepository.findByDoctorOrderByAppointmentDateTimeAsc(doctor);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsForPatient(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));
        return appointmentRepository.findByPatientOrderByAppointmentDateTimeDesc(patient);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findAll() {
        return appointmentRepository.findAll();
    }
}
