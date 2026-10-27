package com.portfolio.medical.repository;

import com.portfolio.medical.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUser_Username(String username);
    Optional<Doctor> findByUserId(Long userId);
    List<Doctor> findBySpecializationIgnoreCase(String specialization);

    @Query("SELECT DISTINCT d.specialization FROM Doctor d ORDER BY d.specialization ASC")
    List<String> findAllSpecializations();
}
