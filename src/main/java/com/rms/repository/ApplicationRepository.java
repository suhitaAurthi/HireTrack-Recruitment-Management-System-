package com.rms.repository;

import com.rms.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByCandidateProfileIdAndJobId(Long candidateProfileId, Long jobId);

    List<Application> findByCandidateProfileIdOrderByAppliedAtDesc(Long candidateProfileId);

    List<Application> findByJobIdOrderByAppliedAtDesc(Long jobId);

    @Query("SELECT a FROM Application a JOIN FETCH a.job JOIN FETCH a.candidateProfile cp JOIN FETCH cp.user WHERE a.id = :id")
    Optional<Application> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT a FROM Application a JOIN FETCH a.job JOIN FETCH a.candidateProfile cp JOIN FETCH cp.user ORDER BY a.appliedAt DESC")
    List<Application> findAllWithDetails();

    @Query("SELECT a FROM Application a JOIN FETCH a.job WHERE a.candidateProfile.id = :candidateProfileId ORDER BY a.appliedAt DESC")
    List<Application> findByCandidateProfileIdWithJob(@Param("candidateProfileId") Long candidateProfileId);

    @Query("SELECT a FROM Application a JOIN FETCH a.candidateProfile cp JOIN FETCH cp.user WHERE a.job.id = :jobId ORDER BY a.appliedAt DESC")
    List<Application> findByJobIdWithCandidate(@Param("jobId") Long jobId);
}
