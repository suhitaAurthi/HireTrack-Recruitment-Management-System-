package com.rms.config;

import com.rms.entity.Application;
import com.rms.entity.ApplicationStatus;
import com.rms.entity.CandidateProfile;
import com.rms.entity.Job;
import com.rms.entity.Role;
import com.rms.entity.RoleType;
import com.rms.entity.User;
import com.rms.repository.ApplicationRepository;
import com.rms.repository.CandidateProfileRepository;
import com.rms.repository.JobRepository;
import com.rms.repository.RoleRepository;
import com.rms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * DataInitializer: Seeds initial system roles, default users, and sample candidate profile.
 * Enables instant demo login for evaluators and developers without manual registration.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initializing HireTrack default roles and demo accounts...");

        // 1. Seed Roles
        Role adminRole = getOrCreateRole(RoleType.ROLE_ADMIN);
        Role recruiterRole = getOrCreateRole(RoleType.ROLE_RECRUITER);
        Role candidateRole = getOrCreateRole(RoleType.ROLE_CANDIDATE);

        // 2. Seed Admin User: admin / admin123
        if (!userRepository.existsByUsername("admin")) {
            userRepository.save(User.builder()
                    .username("admin")
                    .email("admin@hiretrack.com")
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("System")
                    .lastName("Administrator")
                    .enabled(true)
                    .roles(Set.of(adminRole))
                    .build());
            log.info("Created demo user: admin (password: admin123)");
        }

        // 3. Seed Recruiter User: recruiter / recruiter123
        if (!userRepository.existsByUsername("recruiter")) {
            userRepository.save(User.builder()
                    .username("recruiter")
                    .email("recruiter@hiretrack.com")
                    .password(passwordEncoder.encode("recruiter123"))
                    .firstName("Alex")
                    .lastName("Mercer")
                    .enabled(true)
                    .roles(Set.of(recruiterRole))
                    .build());
            log.info("Created demo user: recruiter (password: recruiter123)");
        }

        // 4. Seed Candidate User: candidate / candidate123
        if (!userRepository.existsByUsername("candidate")) {
            User candidateUser = userRepository.save(User.builder()
                    .username("candidate")
                    .email("candidate@hiretrack.com")
                    .password(passwordEncoder.encode("candidate123"))
                    .firstName("Sarah")
                    .lastName("Connor")
                    .enabled(true)
                    .roles(Set.of(candidateRole))
                    .build());

            // 5. Seed Pre-populated Candidate Profile
            candidateProfileRepository.save(CandidateProfile.builder()
                    .user(candidateUser)
                    .headline("Senior Full-Stack Engineer | Java, Spring Boot & Cloud Architecture")
                    .phone("+1 (555) 234-5678")
                    .location("San Francisco, CA (Open to Remote)")
                    .summary("Experienced software engineer with 5+ years designing enterprise microservices, high-throughput REST APIs, and event-driven architectures. Passionate about clean code, test-driven development, and scalable cloud solutions.")
                    .skills(new HashSet<>(Set.of("Java", "Spring Boot", "PostgreSQL", "Docker", "Kubernetes", "REST APIs", "Git", "Microservices")))
                    .education("B.S. in Computer Science & Engineering\nStanford University (2018 - 2022)")
                    .experience("Senior Software Engineer at Horizon Cloud (2022 - Present)\n- Engineered distributed microservices reducing query latency by 40%\n- Implemented OAuth2 and Spring Security RBAC across 8 core services\n- Mentored 4 junior engineers in CI/CD and Dockerization best practices")
                    .build());

            log.info("Created demo user: candidate (password: candidate123) with pre-populated profile");
        }

        // 5. Seed Second Demo Candidate: dev_marcus / marcus123
        if (!userRepository.existsByUsername("dev_marcus")) {
            User candidateUser2 = userRepository.save(User.builder()
                    .username("dev_marcus")
                    .email("marcus.chen@hiretrack.com")
                    .password(passwordEncoder.encode("marcus123"))
                    .firstName("Marcus")
                    .lastName("Chen")
                    .enabled(true)
                    .roles(Set.of(candidateRole))
                    .build());

            candidateProfileRepository.save(CandidateProfile.builder()
                    .user(candidateUser2)
                    .headline("Staff Cloud & DevOps Architect | AWS, Kubernetes & Terraform")
                    .phone("+1 (555) 890-1234")
                    .location("Seattle, WA (Remote)")
                    .summary("Cloud infrastructure engineer with 7+ years orchestrating multi-region Kubernetes clusters, automated zero-downtime CI/CD pipelines, and infrastructure-as-code.")
                    .skills(new HashSet<>(Set.of("Kubernetes", "AWS", "Terraform", "Docker", "Java", "Go", "CI/CD", "Prometheus")))
                    .education("M.S. in Computer Systems\nUniversity of Washington (2016 - 2018)")
                    .experience("Staff Infrastructure Engineer at CloudScale Inc (2020 - Present)\n- Managed 120-node EKS cluster with 99.99% uptime\n- Cut AWS infrastructure spend by 35% through spot instances and auto-scaling")
                    .build());

            log.info("Created demo user: dev_marcus (password: marcus123) with pre-populated profile");
        }

        // 6. Seed Demo Job Vacancies (Coordinating with Module C / Member 1)
        Job job1 = null;
        Job job2 = null;
        if (jobRepository.count() == 0) {
            job1 = jobRepository.save(Job.builder()
                    .title("Senior Java & Cloud Architect")
                    .department("Core Platform Engineering")
                    .location("San Francisco, CA (Remote Friendly)")
                    .employmentType("Full-Time")
                    .description("Lead the design and evolution of high-throughput distributed microservices on Spring Boot 3, Kubernetes, and PostgreSQL.")
                    .requirements("5+ years of Java/Spring ecosystem, RESTful API design, Docker/Kubernetes container orchestration, and relational database optimization.")
                    .category("Software Engineering")
                    .salaryMin(new BigDecimal("140000"))
                    .salaryMax(new BigDecimal("175000"))
                    .applicationDeadline(LocalDate.now().plusDays(30))
                    .openPositions(2)
                    .educationRequirement("B.Sc. in Computer Science or a related field")
                    .minExperienceYears(5)
                    .requiredSkills(new HashSet<>(Set.of("Java", "Spring Boot", "PostgreSQL", "Docker")))
                    .active(true)
                    .build());

            job2 = jobRepository.save(Job.builder()
                    .title("Staff Cloud & DevOps Engineer")
                    .department("Cloud Infrastructure")
                    .location("Seattle, WA (Remote)")
                    .employmentType("Full-Time")
                    .description("Architect scalable cloud infrastructure, zero-downtime CI/CD deployment pipelines, and observability monitoring with Prometheus & Grafana.")
                    .requirements("Strong background in AWS/GCP, Terraform IaC, multi-cluster Kubernetes administration, and automated test pipelines.")
                    .category("DevOps")
                    .salaryMin(new BigDecimal("150000"))
                    .salaryMax(new BigDecimal("185000"))
                    .applicationDeadline(LocalDate.now().plusDays(45))
                    .openPositions(1)
                    .educationRequirement("B.Sc. in Computer Science or equivalent experience")
                    .minExperienceYears(6)
                    .requiredSkills(new HashSet<>(Set.of("Kubernetes", "AWS", "Terraform", "CI/CD")))
                    .active(true)
                    .build());

            log.info("Seeded initial active job vacancies for application management testing");
        }

        // 7. Seed Demo Applications for Candidate Pipeline Testing
        if (applicationRepository.count() == 0 && job1 != null && job2 != null) {
            CandidateProfile candidateProfile1 = candidateProfileRepository.findByUserUsername("candidate").orElse(null);
            CandidateProfile candidateProfile2 = candidateProfileRepository.findByUserUsername("dev_marcus").orElse(null);

            if (candidateProfile1 != null) {
                applicationRepository.save(Application.builder()
                        .candidateProfile(candidateProfile1)
                        .job(job1)
                        .status(ApplicationStatus.UNDER_REVIEW)
                        .coverLetter("Excited to bring 5+ years of Java/Spring microservices and cloud experience to HireTrack.")
                        .recruiterNotes("Profile matches key technical requirements. Reviewing CV.")
                        .build());
            }

            if (candidateProfile2 != null) {
                applicationRepository.save(Application.builder()
                        .candidateProfile(candidateProfile2)
                        .job(job2)
                        .status(ApplicationStatus.SHORTLISTED)
                        .coverLetter("Passionate about Kubernetes orchestration, zero-downtime deployments, and infrastructure-as-code.")
                        .recruiterNotes("Strong Kubernetes and Terraform background. Shortlisted for technical round.")
                        .build());
            }

            log.info("Seeded initial demo job applications for pipeline and status tracking");
        }
    }

    private Role getOrCreateRole(RoleType roleType) {
        return roleRepository.findByName(roleType)
                .orElseGet(() -> roleRepository.save(Role.builder().name(roleType).build()));
    }
}