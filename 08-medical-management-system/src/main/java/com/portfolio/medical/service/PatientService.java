package com.portfolio.medical.service;

import com.portfolio.medical.exception.ResourceNotFoundException;
import com.portfolio.medical.model.Patient;
import com.portfolio.medical.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Transactional(readOnly = true)
    public List<Patient> findAll() {
        return patientRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Patient findById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Patient findByUsername(String username) {
        return patientRepository.findByUser_Username(username)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found for user: " + username));
    }

    public Patient save(Patient patient) {
        return patientRepository.save(patient);
    }
}
