package com.portfolio.medical.repository;

import com.portfolio.medical.model.Doctor;
import com.portfolio.medical.model.Patient;
import com.portfolio.medical.model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByPatientOrderByIssueDateDesc(Patient patient);
    List<Prescription> findByDoctorOrderByIssueDateDesc(Doctor doctor);
    List<Prescription> findByAppointmentId(Long appointmentId);
}
