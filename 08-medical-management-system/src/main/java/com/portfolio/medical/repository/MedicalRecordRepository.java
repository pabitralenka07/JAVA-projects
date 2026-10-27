package com.portfolio.medical.repository;

import com.portfolio.medical.model.Doctor;
import com.portfolio.medical.model.MedicalRecord;
import com.portfolio.medical.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    List<MedicalRecord> findByPatientOrderByRecordDateDesc(Patient patient);
    List<MedicalRecord> findByDoctorOrderByRecordDateDesc(Doctor doctor);
}
