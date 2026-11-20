package com.rms.repository;

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
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("Should save user with role and retrieve by username")
    void shouldSaveAndFindByUsername() {
        // Given
        Role candidateRole = roleRepository.save(
                Role.builder().name(RoleType.ROLE_CANDIDATE).build()
        );

        User user = User.builder()
                .username("john_doe")
                .email("john@example.com")
                .password("hashed_password_123")
                .firstName("John")
                .lastName("Doe")
                .roles(Set.of(candidateRole))
                .build();

        // When
        User savedUser = userRepository.save(user);
        Optional<User> found = userRepository.findByUsername("john_doe");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isNotNull();
        assertThat(found.get().getEmail()).isEqualTo("john@example.com");
        assertThat(found.get().getRoles()).hasSize(1);
        assertThat(found.get().getRoles().iterator().next().getName()).isEqualTo(RoleType.ROLE_CANDIDATE);
    }

    @Test
    @DisplayName("Should verify user existence by username and email")
    void shouldCheckExistsByUsernameAndEmail() {
        // Given
        User user = User.builder()
                .username("alice")
                .email("alice@example.com")
                .password("secretPass")
                .firstName("Alice")
                .lastName("Smith")
                .build();
        userRepository.save(user);

        // When & Then
        assertThat(userRepository.existsByUsername("alice")).isTrue();
        assertThat(userRepository.existsByUsername("unknown_user")).isFalse();
        assertThat(userRepository.existsByEmail("alice@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("unknown@example.com")).isFalse();
    }
}
