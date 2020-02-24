package com.github.kurkov.kafkatestcontainers;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SpringBootTest(classes = Application.class)
@ContextConfiguration(initializers = {AbstractTest.ContextInitializer.class})
public abstract class AbstractTest {

    protected static Logger logger = LoggerFactory.getLogger(AbstractTest.class);

    protected static GenericContainer zookeeper;
    protected static KafkaContainer kafka;

    @BeforeClass
    public static void startContainers() {
        Network network = Network.newNetwork();

        kafka = new KafkaContainer()
                .withNetwork(network)
                .withNetworkAliases("kafka")
                .withExternalZookeeper("zookeeper:2181");

        zookeeper = new GenericContainer<>("confluentinc/cp-zookeeper:4.0.0")
                .withNetwork(network)
                .withNetworkAliases("zookeeper")
                .withEnv("ZOOKEEPER_CLIENT_PORT", "2181")
                .withExposedPorts(2181);

        logger.info("Starting local Zookeeper in docker container...");
        zookeeper.start();
        assertTrue(zookeeper.isRunning());
        logger.info("Zookeeper has been started!");

        String zookeeperAddress = zookeeper.getContainerIpAddress() + ":" + zookeeper.getFirstMappedPort();
        logger.info("Zookeeper address: " + zookeeperAddress);

        logger.info("Starting local Kafka in docker container...");
        kafka.start();
        assertTrue(kafka.isRunning());
        logger.info("Kafka has been started!");

        String kafkaAddress = kafka.getContainerIpAddress() + ":" + kafka.getFirstMappedPort();
        logger.info("Kafka address: " + kafkaAddress);

        String bootstrapServers = kafka.getBootstrapServers();
        logger.info("Broker address (bootstrap-servers): " + bootstrapServers);
    }

    static class ContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "kafka.server=" + kafka.getBootstrapServers()
            ).applyTo(configurableApplicationContext.getEnvironment());

        }
    }

    @AfterClass
    public static void stopContainers() {
        logger.info("Stopping Kafka docker container...");
        kafka.stop();
        assertFalse(kafka.isRunning());
        logger.info("Kafka has been stopped!");

        logger.info("Stopping Zookeeper docker container...");
        zookeeper.stop();
        assertFalse(zookeeper.isRunning());
        logger.info("Zookeeper has been stopped!");
    }
}
