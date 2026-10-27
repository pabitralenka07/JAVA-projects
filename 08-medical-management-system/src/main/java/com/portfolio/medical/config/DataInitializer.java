package com.portfolio.medical.config;

import com.portfolio.medical.model.*;
import com.portfolio.medical.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           DoctorRepository doctorRepository,
                           PatientRepository patientRepository,
                           AppointmentRepository appointmentRepository,
                           MedicalRecordRepository medicalRecordRepository,
                           PrescriptionRepository prescriptionRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Medical database already initialized. Skipping demo data seeding.");
            return;
        }

        log.info("Initializing demo data for Medical Management System...");

        // 1. Admin User
        User admin = new User(
                "admin",
                passwordEncoder.encode("admin123"),
                "Administrator Sarah Chen",
                "admin@hospital.org",
                "+1-555-0100",
                Role.ROLE_ADMIN
        );
        userRepository.save(admin);

        // 2. Doctors
        User docUser1 = new User(
                "dr_house",
                passwordEncoder.encode("doctor123"),
                "Dr. Gregory House",
                "house@hospital.org",
                "+1-555-0101",
                Role.ROLE_DOCTOR
        );
        Doctor doc1 = new Doctor(
                docUser1,
                "Diagnostic Medicine",
                new BigDecimal("150.00"),
                "Room 404",
                "Monday,Tuesday,Wednesday,Thursday,Friday",
                "Department Head of Diagnostic Medicine with 18+ years of complex case experience."
        );
        doctorRepository.save(doc1);

        User docUser2 = new User(
                "dr_watson",
                passwordEncoder.encode("doctor123"),
                "Dr. John Watson",
                "watson@hospital.org",
                "+1-555-0102",
                Role.ROLE_DOCTOR
        );
        Doctor doc2 = new Doctor(
                docUser2,
                "General Medicine",
                new BigDecimal("80.00"),
                "Room 101",
                "Monday,Wednesday,Friday",
                "Experienced family physician specializing in preventive healthcare and chronic illness."
        );
        doctorRepository.save(doc2);

        User docUser3 = new User(
                "dr_strange",
                passwordEncoder.encode("doctor123"),
                "Dr. Stephen Strange",
                "strange@hospital.org",
                "+1-555-0103",
                Role.ROLE_DOCTOR
        );
        Doctor doc3 = new Doctor(
                docUser3,
                "Neuro-Surgery",
                new BigDecimal("250.00"),
                "Surgical Suite 3",
                "Tuesday,Thursday",
                "Board-certified neurosurgeon focusing on minimally invasive neurological procedures."
        );
        doctorRepository.save(doc3);

        // 3. Patients
        User patUser1 = new User(
                "pat_john",
                passwordEncoder.encode("patient123"),
                "John Doe",
                "john.doe@example.com",
                "+1-555-0201",
                Role.ROLE_PATIENT
        );
        Patient pat1 = new Patient(
                patUser1,
                LocalDate.of(1992, 4, 15),
                "Male",
                "O+",
                "Mary Doe (+1-555-0202)",
                "42 Baker Street, Apt 3B, New York, NY"
        );
        patientRepository.save(pat1);

        User patUser2 = new User(
                "pat_sarah",
                passwordEncoder.encode("patient123"),
                "Sarah Connor",
                "sarah.c@example.com",
                "+1-555-0203",
                Role.ROLE_PATIENT
        );
        Patient pat2 = new Patient(
                patUser2,
                LocalDate.of(1988, 11, 28),
                "Female",
                "A-",
                "John Connor (+1-555-0204)",
                "742 Evergreen Terrace, Los Angeles, CA"
        );
        patientRepository.save(pat2);

        // 4. Appointments
        LocalDateTime nextWeek = LocalDateTime.now().plusDays(3).withHour(10).withMinute(0).withSecond(0).withNano(0);
        Appointment appt1 = new Appointment(pat1, doc1, nextWeek, "Persistent unexplained fever and joint inflammation");
        appt1.setStatus(AppointmentStatus.SCHEDULED);
        appt1.setNotes("Initial diagnostic consultation.");
        appointmentRepository.save(appt1);

        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(14).withMinute(30).withSecond(0).withNano(0);
        Appointment appt2 = new Appointment(pat2, doc2, tomorrow, "Annual physical examination and blood panel");
        appt2.setStatus(AppointmentStatus.CONFIRMED);
        appt2.setNotes("Confirmed by clinic reception.");
        appointmentRepository.save(appt2);

        LocalDateTime lastWeek = LocalDateTime.now().minusDays(5).withHour(11).withMinute(0).withSecond(0).withNano(0);
        Appointment appt3 = new Appointment(pat1, doc2, lastWeek, "Respiratory infection and wheezing");
        appt3.setStatus(AppointmentStatus.COMPLETED);
        appt3.setNotes("Completed consultation. Prescribed antibiotic course.");
        appointmentRepository.save(appt3);

        // 5. Medical Record
        MedicalRecord record1 = new MedicalRecord(
                pat1,
                doc2,
                LocalDate.now().minusDays(5),
                "Acute Upper Respiratory Tract Infection",
                "Dry hacking cough, nasal congestion, low-grade fever (100.2 F), fatigue for 4 days.",
                "Course of Amoxicillin 500mg, oral hydration, rest, steam inhalation 2x daily.",
                "Lungs clear on auscultation. Vitals: BP 120/80, HR 72, SpO2 98%."
        );
        medicalRecordRepository.save(record1);

        // 6. Prescription
        Prescription presc1 = new Prescription(
                appt3,
                pat1,
                doc2,
                "1. Amoxicillin 500mg capsules (15 caps)\n2. Paracetamol 650mg tablets (10 tabs)\n3. Cetirizine 10mg tablets (5 tabs)",
                "1. Amoxicillin: Take 1 capsule 3x daily after meals for 5 days.\n2. Paracetamol: Take 1 tablet as needed for fever/headache.\n3. Cetirizine: Take 1 tablet at bedtime for 5 days.",
                "Complete the full antibiotic regimen even if symptoms resolve earlier.",
                LocalDate.now().minusDays(5)
        );
        prescriptionRepository.save(presc1);

        log.info("Medical Management System demo data seeded successfully!");
        log.info("Accounts: [admin: admin123], [dr_house / dr_watson / dr_strange: doctor123], [pat_john / pat_sarah: patient123]");
    }
}
