package com.dotbrains.janus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Janus Application Tests")
class JanusApplicationTest {

    @Test
    @DisplayName("Should create application instance")
    void shouldCreateApplicationInstance() {
        // Test that the main class exists and can be instantiated
        JanusApplication app = new JanusApplication();
        assertThat(app).isNotNull();
    }
}
