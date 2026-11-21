package com.rms.pattern.flyweight;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Flyweight Pattern: Intrinsic State
 * Represents an immutable, canonical skill shared across all candidate profiles.
 */
@Getter
@EqualsAndHashCode(of = "canonicalName")
public class Skill {

    private final String canonicalName;
    private final String category;

    public Skill(String canonicalName, String category) {
        this.canonicalName = canonicalName;
        this.category = category;
    }
}