package com.recruitcrm.candidatemanager.config;

import com.recruitcrm.candidatemanager.entity.Candidate;
import com.recruitcrm.candidatemanager.entity.CandidateStatus;
import com.recruitcrm.candidatemanager.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Populates the database with sample candidates when the "dev" profile is active.
 * Provides a quick way to verify the app works without manual data entry.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final CandidateRepository repository;

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            log.info("Database already seeded — skipping");
            return;
        }

        List<Candidate> candidates = List.of(
            Candidate.builder()
                .firstName("Alice").lastName("Johnson")
                .email("alice.johnson@example.com").phone("+1-555-0101")
                .currentCompany("TechCorp").currentTitle("Senior Java Developer")
                .status(CandidateStatus.NEW)
                .yearsOfExperience(8).expectedSalary("$130,000")
                .skills(List.of("Java", "Spring Boot", "Microservices", "AWS"))
                .notes("Strong backend experience, looking for remote roles.")
                .build(),
            Candidate.builder()
                .firstName("Bob").lastName("Smith")
                .email("bob.smith@example.com").phone("+1-555-0102")
                .currentCompany("DataInc").currentTitle("Full Stack Engineer")
                .status(CandidateStatus.INTERVIEWING)
                .yearsOfExperience(5).expectedSalary("$110,000")
                .skills(List.of("Java", "React", "PostgreSQL", "Docker"))
                .notes("Available to start in 2 weeks.")
                .build(),
            Candidate.builder()
                .firstName("Carol").lastName("Williams")
                .email("carol.williams@example.com").phone("+1-555-0103")
                .currentCompany("StartupXYZ").currentTitle("Backend Engineer")
                .status(CandidateStatus.CONTACTED)
                .yearsOfExperience(3).expectedSalary("$90,000")
                .skills(List.of("Java", "Kotlin", "Spring Boot", "Kubernetes"))
                .build(),
            Candidate.builder()
                .firstName("David").lastName("Brown")
                .email("david.brown@example.com").phone("+1-555-0104")
                .currentCompany("MegaSoft").currentTitle("Principal Engineer")
                .status(CandidateStatus.OFFERED)
                .yearsOfExperience(15).expectedSalary("$180,000")
                .skills(List.of("Java", "System Design", "Leadership", "AWS", "GCP"))
                .notes("Currently negotiating offer terms.")
                .build(),
            Candidate.builder()
                .firstName("Eve").lastName("Davis")
                .email("eve.davis@example.com").phone("+1-555-0105")
                .currentCompany("CloudNine").currentTitle("DevOps Engineer")
                .status(CandidateStatus.HIRED)
                .yearsOfExperience(6).expectedSalary("$120,000")
                .skills(List.of("Java", "CI/CD", "Terraform", "Docker", "Kubernetes"))
                .notes("Joined last month — onboarding complete.")
                .build()
        );

        repository.saveAll(candidates);
        log.info("Seeded {} sample candidates", candidates.size());
    }
}
