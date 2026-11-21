package com.rms.pattern.flyweight;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Flyweight Pattern: Factory Pool
 * Caches and shares canonical Skill instances across all applicants to avoid heap memory bloat.
 */
@Component
public class SkillFlyweightFactory {

    private final Map<String, Skill> pool = new ConcurrentHashMap<>();

    public Skill getSkill(String rawName) {
        if (rawName == null || rawName.trim().isEmpty()) {
            throw new IllegalArgumentException("Skill name cannot be blank");
        }

        String normalized = rawName.trim();
        String lookupKey = normalized.toLowerCase();

        return pool.computeIfAbsent(lookupKey, k -> new Skill(normalized, determineCategory(lookupKey)));
    }

    public int getCachedSkillCount() {
        return pool.size();
    }

    private String determineCategory(String key) {
        if (key.contains("java") || key.contains("python") || key.contains("c++") || key.contains("sql")) {
            return "Programming Language";
        } else if (key.contains("spring") || key.contains("react") || key.contains("angular") || key.contains("vue")) {
            return "Framework";
        } else if (key.contains("docker") || key.contains("kubernetes") || key.contains("aws") || key.contains("git")) {
            return "DevOps & Tools";
        }
        return "General";
    }
}