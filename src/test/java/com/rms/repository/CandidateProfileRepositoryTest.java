package com.rms.repository;

import com.rms.entity.CandidateProfile;
import com.rms.entity.Role;
import com.rms.entity.RoleType;
import com.rms.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CandidateProfileRepositoryTest {

    @Autowired
    private CandidateProfileRepository candidateProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("Should save candidate profile with skills and retrieve by username")
    void shouldSaveAndFindByUsername() {
        Role candidateRole = roleRepository.save(Role.builder().name(RoleType.ROLE_CANDIDATE).build());

        User user = userRepository.save(User.builder()
                .username("sarah_connor")
                .email("sarah@rms.com")
                .password("hashed_secret")
                .firstName("Sarah")
                .lastName("Connor")
                .roles(Set.of(candidateRole))
                .build());

        CandidateProfile profile = CandidateProfile.builder()
                .user(user)
                .headline("Software Engineer")
                .summary("Passionate backend developer with Spring Boot experience.")
                .phone("+123456789")
                .location("Dhaka, Bangladesh")
                .skills(Set.of("Java", "Spring Boot", "PostgreSQL"))
                .education("B.Sc in Computer Science")
                .experience("2 years at Tech Corp")
                .build();

        candidateProfileRepository.save(profile);

        Optional<CandidateProfile> found = candidateProfileRepository.findByUserUsername("sarah_connor");

        assertThat(found).isPresent();
        assertThat(found.get().getHeadline()).isEqualTo("Software Engineer");
        assertThat(found.get().getSkills()).contains("Java", "Spring Boot", "PostgreSQL");
        assertThat(found.get().getUser().getEmail()).isEqualTo("sarah@rms.com");
        assertThat(candidateProfileRepository.existsByUserId(user.getId())).isTrue();
    }
}