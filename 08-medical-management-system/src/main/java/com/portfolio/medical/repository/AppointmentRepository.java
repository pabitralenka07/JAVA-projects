package com.portfolio.medical.repository;

import com.portfolio.medical.model.Appointment;
import com.portfolio.medical.model.AppointmentStatus;
import com.portfolio.medical.model.Doctor;
import com.portfolio.medical.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByDoctorOrderByAppointmentDateTimeAsc(Doctor doctor);
    List<Appointment> findByPatientOrderByAppointmentDateTimeDesc(Patient patient);
    List<Appointment> findByDoctorAndAppointmentDateTimeBetween(Doctor doctor, LocalDateTime start, LocalDateTime end);
    List<Appointment> findByPatientAndAppointmentDateTimeBetween(Patient patient, LocalDateTime start, LocalDateTime end);

    boolean existsByDoctorAndAppointmentDateTimeAndStatusNot(Doctor doctor, LocalDateTime appointmentDateTime, AppointmentStatus status);
}
