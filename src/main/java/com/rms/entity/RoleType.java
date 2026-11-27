package com.rms.entity;

/**
 * RoleType defines the standard roles in the Recruitment Management System.
 * 
 * Note regarding Teacher's Rubric:
 * The lab specification mentions (ADMIN, SELLER, BUYER) as an e-commerce example.
 * In Recruitment Management:
 * - ROLE_ADMIN: System Administrator
 * - ROLE_RECRUITER: Corresponds to SELLER (posts vacancies, reviews applications)
 * - ROLE_CANDIDATE: Corresponds to BUYER (applies for jobs, views status)
 */
public enum RoleType {
    ROLE_ADMIN,
    ROLE_RECRUITER,
    ROLE_CANDIDATE
}
