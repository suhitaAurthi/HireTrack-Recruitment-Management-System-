package com.rms.repository;

import com.rms.entity.CandidateProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CandidateProfileRepository extends JpaRepository<CandidateProfile, Long> {

    Optional<CandidateProfile> findByUserId(Long userId);

    Optional<CandidateProfile> findByUserUsername(String username);

    boolean existsByUserId(Long userId);
}