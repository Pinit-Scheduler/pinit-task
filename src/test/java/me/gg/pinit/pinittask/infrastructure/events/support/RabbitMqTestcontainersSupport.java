package me.gg.pinit.pinittask.infrastructure.events.support;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public abstract class RabbitMqTestcontainersSupport {
    protected static final long MESSAGE_TIMEOUT_MS = 5_000L;
    protected static final long NO_MESSAGE_TIMEOUT_MS = 1_500L;
    private static final DockerImageName RABBIT_IMAGE = DockerImageName.parse("rabbitmq:3.13-management-alpine");
    protected static final RabbitMQContainer rabbitMQContainer = new RabbitMQContainer(RABBIT_IMAGE);
    private static final Path CONTAINER_LOG_PATH = Path.of("build", "testcontainers-logs", "rabbitmq-container.log");
    private static volatile boolean rabbitAvailable;
    private static volatile String unavailableReason = "not checked";

    @DynamicPropertySource
    static void configureRabbitProperties(DynamicPropertyRegistry registry) {
        try {
            if (System.getProperty("api.version") == null || System.getProperty("api.version").isBlank()) {
                System.setProperty("api.version", "1.44");
            }

            if (!DockerClientFactory.instance().isDockerAvailable()) {
                rabbitAvailable = false;
                unavailableReason = "Docker/Testcontainers is not available in this environment";
                System.err.println("[RabbitMQ Testcontainers] " + unavailableReason);
                return;
            }
            if (!rabbitMQContainer.isRunning()) {
                rabbitMQContainer.start();
            }

            rabbitAvailable = true;
            unavailableReason = "";
            registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
            registry.add("spring.rabbitmq.port", rabbitMQContainer::getAmqpPort);
            registry.add("spring.rabbitmq.username", rabbitMQContainer::getAdminUsername);
            registry.add("spring.rabbitmq.password", rabbitMQContainer::getAdminPassword);
        } catch (Throwable throwable) {
            rabbitAvailable = false;
            unavailableReason = throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
            System.err.println("[RabbitMQ Testcontainers] " + unavailableReason);
        }
    }

    @AfterAll
    static void tearDownRabbitContainer() {
        writeContainerLogs();
        if (rabbitMQContainer.isRunning()) {
            rabbitMQContainer.stop();
        }
    }

    private static void writeContainerLogs() {
        if (!rabbitMQContainer.isRunning()) {
            return;
        }

        try {
            Files.createDirectories(CONTAINER_LOG_PATH.getParent());
            Files.writeString(
                    CONTAINER_LOG_PATH,
                    rabbitMQContainer.getLogs(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
            System.err.println("[RabbitMQ Testcontainers] failed to write container logs: " + e.getMessage());
        }
    }

    protected void assumeRabbitAvailable() {
        Assumptions.assumeTrue(rabbitAvailable, "RabbitMQ integration test skipped: " + unavailableReason);
    }
}
