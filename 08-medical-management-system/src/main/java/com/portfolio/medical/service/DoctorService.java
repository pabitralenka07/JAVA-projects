package com.portfolio.medical.service;

import com.portfolio.medical.exception.ResourceNotFoundException;
import com.portfolio.medical.model.Doctor;
import com.portfolio.medical.repository.DoctorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @Transactional(readOnly = true)
    public List<Doctor> findAll() {
        return doctorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Doctor findById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Doctor findByUsername(String username) {
        return doctorRepository.findByUser_Username(username)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found for user: " + username));
    }

    @Transactional(readOnly = true)
    public List<Doctor> findBySpecialization(String specialization) {
        if (specialization == null || specialization.isBlank() || specialization.equalsIgnoreCase("ALL")) {
            return doctorRepository.findAll();
        }
        return doctorRepository.findBySpecializationIgnoreCase(specialization.trim());
    }

    @Transactional(readOnly = true)
    public List<String> getAllSpecializations() {
        return doctorRepository.findAllSpecializations();
    }

    public Doctor save(Doctor doctor) {
        return doctorRepository.save(doctor);
    }
}
