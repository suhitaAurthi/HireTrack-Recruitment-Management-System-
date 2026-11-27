package com.rms.pattern.flyweight;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SkillFlyweightFactoryTest {

    @Test
    @DisplayName("Flyweight: Multiple requests for 'Java' should return the exact same cached instance")
    void shouldReturnSameFlyweightInstance() {
        SkillFlyweightFactory factory = new SkillFlyweightFactory();

        Skill skill1 = factory.getSkill("Java");
        Skill skill2 = factory.getSkill("java");
        Skill skill3 = factory.getSkill("  JAVA  ");

        assertThat(skill1).isSameAs(skill2);
        assertThat(skill2).isSameAs(skill3);
        assertThat(factory.getCachedSkillCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Flyweight: Different skills should create distinct pooled entries")
    void shouldPoolDistinctSkills() {
        SkillFlyweightFactory factory = new SkillFlyweightFactory();

        Skill java = factory.getSkill("Java");
        Skill spring = factory.getSkill("Spring Boot");
        Skill docker = factory.getSkill("Docker");

        assertThat(factory.getCachedSkillCount()).isEqualTo(3);
        assertThat(java.getCategory()).isEqualTo("Programming Language");
        assertThat(spring.getCategory()).isEqualTo("Framework");
        assertThat(docker.getCategory()).isEqualTo("DevOps & Tools");
    }
}