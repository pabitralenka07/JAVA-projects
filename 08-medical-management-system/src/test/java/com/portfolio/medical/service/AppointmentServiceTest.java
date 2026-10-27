package com.portfolio.medical.service;

import com.portfolio.medical.exception.AppointmentConflictException;
import com.portfolio.medical.exception.InvalidStatusTransitionException;
import com.portfolio.medical.model.*;
import com.portfolio.medical.repository.AppointmentRepository;
import com.portfolio.medical.repository.DoctorRepository;
import com.portfolio.medical.repository.PatientRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private Doctor doctor;
    private Patient patient;
    private LocalDateTime appointmentTime;

    @BeforeEach
    void setUp() {
        User docUser = new User("dr_house", "secret", "Dr. Gregory House", "house@hospital.org", "555-0100", Role.ROLE_DOCTOR);
        docUser.setId(10L);
        doctor = new Doctor(docUser, "Diagnostic Medicine", new BigDecimal("150.00"), "Room 404", "Mon,Wed,Fri", "Head of Diagnostic Medicine");
        doctor.setId(1L);

        User patUser = new User("john_doe", "secret", "John Doe", "john@example.com", "555-0200", Role.ROLE_PATIENT);
        patUser.setId(20L);
        patient = new Patient(patUser, LocalDate.of(1990, 5, 15), "Male", "O+", "Jane Doe (555-0201)", "42 Baker St");
        patient.setId(2L);

        appointmentTime = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0);
    }

    @Test
    @DisplayName("Should successfully schedule an appointment when slot is available")
    void testScheduleAppointmentSuccess() {
        when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.existsByDoctorAndAppointmentDateTimeAndStatusNot(doctor, appointmentTime, AppointmentStatus.CANCELLED))
                .thenReturn(false);

        Appointment savedAppointment = new Appointment(patient, doctor, appointmentTime, "Persistent cough");
        savedAppointment.setId(100L);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(savedAppointment);

        Appointment result = appointmentService.scheduleAppointment(2L, 1L, appointmentTime, "Persistent cough");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(result.getDoctor().getUser().getFullName()).isEqualTo("Dr. Gregory House");
        assertThat(result.getPatient().getUser().getFullName()).isEqualTo("John Doe");

        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should reject double-booking for the same doctor at the same slot")
    void testScheduleAppointmentRejectsDoubleBooking() {
        when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.existsByDoctorAndAppointmentDateTimeAndStatusNot(doctor, appointmentTime, AppointmentStatus.CANCELLED))
                .thenReturn(true);

        assertThatThrownBy(() -> appointmentService.scheduleAppointment(2L, 1L, appointmentTime, "Second booking attempt"))
                .isInstanceOf(AppointmentConflictException.class)
                .hasMessageContaining("already booked");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should validate valid status transitions SCHEDULED -> CONFIRMED -> COMPLETED")
    void testAppointmentStatusTransitions() {
        Appointment appointment = new Appointment(patient, doctor, appointmentTime, "Annual Checkup");
        appointment.setId(101L);
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        when(appointmentRepository.findById(101L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Step 1: Confirm
        Appointment confirmed = appointmentService.confirmAppointment(101L);
        assertThat(confirmed.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);

        // Step 2: Complete
        Appointment completed = appointmentService.completeAppointment(101L, "Patient healthy, vitals normal.");
        assertThat(completed.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
        assertThat(completed.getNotes()).contains("Patient healthy, vitals normal.");
    }

    @Test
    @DisplayName("Should reject illegal status transitions (e.g. COMPLETED -> CANCELLED)")
    void testInvalidStatusTransitionThrowsException() {
        Appointment appointment = new Appointment(patient, doctor, appointmentTime, "Checkup");
        appointment.setId(102L);
        appointment.setStatus(AppointmentStatus.COMPLETED);

        when(appointmentRepository.findById(102L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.cancelAppointment(102L, "Tried to cancel after completion"))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("Cannot transition appointment #102 from COMPLETED to CANCELLED");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should cancel an active appointment and append cancellation reason to notes")
    void testCancelAppointmentWithReason() {
        Appointment appointment = new Appointment(patient, doctor, appointmentTime, "Consultation");
        appointment.setId(103L);
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        when(appointmentRepository.findById(103L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Appointment cancelled = appointmentService.cancelAppointment(103L, "Patient caught in traffic");

        assertThat(cancelled.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(cancelled.getNotes()).contains("Cancellation reason: Patient caught in traffic");
    }
}
