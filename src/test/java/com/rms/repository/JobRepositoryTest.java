package com.rms.repository;

import com.rms.entity.Job;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class JobRepositoryTest {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should save and reload vacancy details and requirements")
    void shouldPersistVacancyDetailsAndRequirements() {
        LocalDate deadline = LocalDate.of(2030, 1, 31);
        Job job = jobBuilder()
                .category("Software Engineering")
                .salaryMin(new BigDecimal("50000.00"))
                .salaryMax(new BigDecimal("70000.00"))
                .applicationDeadline(deadline)
                .openPositions(3)
                .educationRequirement("B.Sc. in CSE")
                .minExperienceYears(2)
                .requiredSkills(Set.of("Java", "Spring Boot"))
                .build();

        Long id = saveAndClear(job);
        Job reloaded = jobRepository.findById(id).orElseThrow();

        assertThat(reloaded.getCategory()).isEqualTo("Software Engineering");
        assertThat(reloaded.getSalaryMin()).isEqualByComparingTo("50000.00");
        assertThat(reloaded.getSalaryMax()).isEqualByComparingTo("70000.00");
        assertThat(reloaded.getApplicationDeadline()).isEqualTo(deadline);
        assertThat(reloaded.getOpenPositions()).isEqualTo(3);
        assertThat(reloaded.getEducationRequirement()).isEqualTo("B.Sc. in CSE");
        assertThat(reloaded.getMinExperienceYears()).isEqualTo(2);
        assertThat(reloaded.getRequiredSkills()).containsExactlyInAnyOrder("Java", "Spring Boot");
    }

    @Test
    @DisplayName("Should default to one open position and no required skills")
    void shouldApplyDefaultsWhenRequirementsAreOmitted() {
        Long id = saveAndClear(jobBuilder().build());
        Job reloaded = jobRepository.findById(id).orElseThrow();

        assertThat(reloaded.getOpenPositions()).isEqualTo(1);
        assertThat(reloaded.getRequiredSkills()).isEmpty();
        assertThat(reloaded.getApplicationDeadline()).isNull();
    }

    private Job.JobBuilder jobBuilder() {
        return Job.builder()
                .title("Backend Engineer")
                .department("Engineering")
                .location("Khulna")
                .employmentType("Full-Time")
                .description("Build REST APIs with Spring Boot.");
    }

    /** Flushes and detaches so the following read comes from the database. */
    private Long saveAndClear(Job job) {
        Long id = jobRepository.save(job).getId();
        entityManager.flush();
        entityManager.clear();
        return id;
    }
}
