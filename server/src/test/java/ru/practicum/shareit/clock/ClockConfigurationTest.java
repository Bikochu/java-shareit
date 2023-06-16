package ru.practicum.shareit.clock;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClockConfigurationTest {

    @Test
    void clockReturnsSystemUTCTime() {
        // Arrange
        ClockConfiguration clockConfiguration = new ClockConfiguration();

        // Act
        Clock clock = clockConfiguration.clock();

        // Assert
        Instant expectedInstant = Instant.now();
        Instant actualInstant = clock.instant();

        assertEquals(expectedInstant, actualInstant);
    }

}