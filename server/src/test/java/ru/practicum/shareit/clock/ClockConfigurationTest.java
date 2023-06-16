package ru.practicum.shareit.clock;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ClockConfigurationTest {

    @Test
    void clockReturnsSystemUTCTime() {
        ClockConfiguration clockConfiguration = new ClockConfiguration();

        Clock clock = clockConfiguration.clock();

        Instant actualInstant = clock.instant();

        assertNotNull(actualInstant);
    }

}