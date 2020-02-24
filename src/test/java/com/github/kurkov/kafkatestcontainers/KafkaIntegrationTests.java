package com.github.kurkov.kafkatestcontainers;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class KafkaIntegrationTests extends AbstractTest {

    @Test
    public void contextLoads() {
        assertFalse("kafka.server=localhost:9092".equals(kafka.getBootstrapServers()));
    }
}
