package com.rms.repository;

import com.rms.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class ApplicationRepositoryTest {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private TestEntityManager entityManager;

    private CandidateProfile candidateProfile;
    private Job job;

    @BeforeEach
    void setUp() {
        Role role = entityManager.persist(Role.builder().name(RoleType.ROLE_CANDIDATE).build());
        User user = entityManager.persist(User.builder()
                .username("test_applicant")
                .email("applicant@test.com")
                .password("encoded_pass")
                .enabled(true)
                .roles(Set.of(role))
                .build());

        candidateProfile = entityManager.persist(CandidateProfile.builder()
                .user(user)
                .headline("Java Developer")
                .location("Remote")
                .build());

        job = entityManager.persist(Job.builder()
                .title("Senior Backend Engineer")
                .department("Engineering")
                .location("Remote")
                .employmentType("Full-Time")
                .description("Build scalable microservices with Spring Boot.")
                .active(true)
                .build());

        entityManager.flush();
    }

    @Test
    @DisplayName("Should successfully persist job application with default SUBMITTED status")
    void shouldPersistApplication() {
        Application application = Application.builder()
                .candidateProfile(candidateProfile)
                .job(job)
                .coverLetter("Excited to apply for this engineering role.")
                .build();

        Application saved = applicationRepository.save(application);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(ApplicationStatus.SUBMITTED);
        assertThat(saved.getCandidateProfile().getId()).isEqualTo(candidateProfile.getId());
        assertThat(saved.getJob().getId()).isEqualTo(job.getId());
    }

    @Test
    @DisplayName("Database Unique Constraint: Should prevent duplicate applications from same candidate for same job")
    void shouldPreventDuplicateApplicationsForSameJob() {
        Application firstApplication = Application.builder()
                .candidateProfile(candidateProfile)
                .job(job)
                .coverLetter("First application attempt")
                .build();
        applicationRepository.saveAndFlush(firstApplication);

        Application duplicateApplication = Application.builder()
                .candidateProfile(candidateProfile)
                .job(job)
                .coverLetter("Duplicate attempt")
                .build();

        assertThatThrownBy(() -> {
            applicationRepository.saveAndFlush(duplicateApplication);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("existsByCandidateProfileIdAndJobId should return true when application exists and false otherwise")
    void shouldCheckApplicationExistence() {
        assertThat(applicationRepository.existsByCandidateProfileIdAndJobId(candidateProfile.getId(), job.getId()))
                .isFalse();

        Application application = Application.builder()
                .candidateProfile(candidateProfile)
                .job(job)
                .build();
        applicationRepository.save(application);

        assertThat(applicationRepository.existsByCandidateProfileIdAndJobId(candidateProfile.getId(), job.getId()))
                .isTrue();
    }

    @Test
    @DisplayName("Should find applications by candidateProfileId")
    void shouldFindByCandidateProfileId() {
        Application application = Application.builder()
                .candidateProfile(candidateProfile)
                .job(job)
                .build();
        applicationRepository.save(application);

        List<Application> found = applicationRepository.findByCandidateProfileIdOrderByAppliedAtDesc(candidateProfile.getId());
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getJob().getTitle()).isEqualTo("Senior Backend Engineer");
    }
}
