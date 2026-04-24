package com.luxury.common.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import java.time.temporal.ChronoUnit;

/**
 * Unit tests for BaseEntity lifecycle callbacks and defaults.
 */
@DisplayName("BaseEntity Unit Tests")
class BaseEntityTest {

    // Concrete subclass for testing the abstract BaseEntity
    static class TestEntity extends BaseEntity {
        private String value;

        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    @Test
    @DisplayName("onCreate should set createdAt and updatedAt to now")
    void onCreateShouldSetTimestamps() {
        TestEntity entity = new TestEntity();
        entity.onCreate();

        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
        assertThat(entity.getCreatedAt()).isCloseTo(Instant.now(), within(2, ChronoUnit.SECONDS));
        assertThat(entity.getCreatedAt()).isEqualTo(entity.getUpdatedAt());
    }

    @Test
    @DisplayName("onUpdate should update only updatedAt")
    void onUpdateShouldRefreshUpdatedAt() throws InterruptedException {
        TestEntity entity = new TestEntity();
        entity.onCreate();

        Instant originalCreatedAt = entity.getCreatedAt();

        // Small delay to ensure timestamps differ
        Thread.sleep(10);
        entity.onUpdate();

        assertThat(entity.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(entity.getUpdatedAt()).isAfterOrEqualTo(originalCreatedAt);
    }

    @Test
    @DisplayName("Default isDeleted should be false")
    void defaultIsDeletedShouldBeFalse() {
        TestEntity entity = new TestEntity();
        assertThat(entity.getIsDeleted()).isFalse();
    }

    @Test
    @DisplayName("Should allow setting all audit fields")
    void shouldSetAuditFields() {
        TestEntity entity = new TestEntity();
        entity.setCreatedBy("associate-001");
        entity.setUpdatedBy("admin-002");
        entity.setIsDeleted(true);

        assertThat(entity.getCreatedBy()).isEqualTo("associate-001");
        assertThat(entity.getUpdatedBy()).isEqualTo("admin-002");
        assertThat(entity.getIsDeleted()).isTrue();
    }
}
