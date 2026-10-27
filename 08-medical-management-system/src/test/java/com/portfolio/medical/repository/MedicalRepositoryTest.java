package com.portfolio.medical.repository;

import com.portfolio.medical.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MedicalRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Test
    @DisplayName("Should persist Doctor and User cascade, and query distinct specializations")
    void testDoctorPersistenceAndSpecializationQuery() {
        User user1 = new User("dr_stephen", "pass", "Dr. Stephen Strange", "strange@hospital.org", "555-9999", Role.ROLE_DOCTOR);
        Doctor doc1 = new Doctor(user1, "Neuro-Surgery", new BigDecimal("250.00"), "OR-1", "Mon-Fri", "Sorcerer Supreme");
        doctorRepository.save(doc1);

        User user2 = new User("dr_mccoy", "pass", "Dr. Leonard McCoy", "mccoy@hospital.org", "555-8888", Role.ROLE_DOCTOR);
        Doctor doc2 = new Doctor(user2, "Cardiology", new BigDecimal("180.00"), "Room 102", "Tue-Thu", "Chief Medical Officer");
        doctorRepository.save(doc2);

        List<String> specializations = doctorRepository.findAllSpecializations();
        assertThat(specializations).containsExactly("Cardiology", "Neuro-Surgery");

        List<Doctor> cardioDocs = doctorRepository.findBySpecializationIgnoreCase("Cardiology");
        assertThat(cardioDocs).hasSize(1);
        assertThat(cardioDocs.get(0).getUser().getFullName()).isEqualTo("Dr. Leonard McCoy");
    }

    @Test
    @DisplayName("Should verify appointment double-booking check query against database")
    void testAppointmentDoubleBookingCheck() {
        User docUser = new User("dr_meredith", "pass", "Dr. Meredith Grey", "grey@seattle.org", "555-7777", Role.ROLE_DOCTOR);
        Doctor doctor = doctorRepository.save(new Doctor(docUser, "General Surgery", new BigDecimal("120.00"), "Room 201", "Mon-Fri", "Surgeon"));

        User patUser = new User("clara_oswald", "pass", "Clara Oswald", "clara@tardis.org", "555-6666", Role.ROLE_PATIENT);
        Patient patient = patientRepository.save(new Patient(patUser, LocalDate.of(1992, 11, 23), "Female", "B+", "Doctor", "London"));

        LocalDateTime slot = LocalDateTime.of(2026, 11, 15, 14, 0);

        // Before booking
        boolean conflictBefore = appointmentRepository.existsByDoctorAndAppointmentDateTimeAndStatusNot(
                doctor, slot, AppointmentStatus.CANCELLED
        );
        assertThat(conflictBefore).isFalse();

        // Save active appointment
        Appointment appt = new Appointment(patient, doctor, slot, "Routine consultation");
        appt.setStatus(AppointmentStatus.SCHEDULED);
        appointmentRepository.save(appt);

        // After booking
        boolean conflictAfter = appointmentRepository.existsByDoctorAndAppointmentDateTimeAndStatusNot(
                doctor, slot, AppointmentStatus.CANCELLED
        );
        assertThat(conflictAfter).isTrue();

        // If appointment is cancelled, slot becomes available
        appt.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appt);

        boolean conflictCancelled = appointmentRepository.existsByDoctorAndAppointmentDateTimeAndStatusNot(
                doctor, slot, AppointmentStatus.CANCELLED
        );
        assertThat(conflictCancelled).isFalse();
    }
}
