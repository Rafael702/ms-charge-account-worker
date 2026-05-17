package com.itau.chargeaccount.infrastructure.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itau.chargeaccount.domain.event.ChargeReceivedEvent;
import com.itau.chargeaccount.domain.valueobject.ChargeId;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("KafkaPublisherAdapter Integration Tests with Testcontainers")
class KafkaPublisherAdapterIT {
    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.5.0")
    );

    @Autowired
    private KafkaPublisherAdapter kafkaPublisherAdapter;

    private KafkaConsumer<String, ChargeReceivedEvent> consumer;

    private String chargeEventsTopic = "charge.events.test";

    private String accountingEventsTopic = "accounting.events.test";
    private ObjectMapper mapper = new ObjectMapper();

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("app.kafka.topics.charge-events", () -> "charge.events.test");
        registry.add("app.kafka.topics.account-status", () -> "account.status.test");
        registry.add("app.kafka.topics.accounting-events", () -> "accounting.events.test");
    }

    @BeforeAll
    static void setupTopics() {
        try (AdminClient admin = AdminClient.create(
                Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers())
        )) {
            admin.createTopics(java.util.Arrays.asList(
                    new NewTopic("charge.events.test", 1, (short) 1),
                    new NewTopic("account.status.test", 1, (short) 1),
                    new NewTopic("accounting.events.test", 1, (short) 1)
            )).all().get();
            log.info("✅ Kafka topics created successfully");
        } catch (Exception e) {
            log.warn("Topics may already exist: {}", e.getMessage());
        }
    }

    @BeforeEach
    void setUp() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group-" + System.nanoTime());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, ChargeReceivedEvent.class);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ChargeReceivedEvent.class.getName());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        consumer = new KafkaConsumer<>(props);
        log.info("✅ Kafka Consumer created with unique group");
    }

    @AfterEach
    void tearDown() {
        if (consumer != null) {
            consumer.close();
            log.info("✅ Kafka Consumer closed");
        }
    }

    @Test
    @DisplayName("Should publish event to charge events topic successfully")
    void shouldPublishEventSuccessfully() {
        ChargeReceivedEvent event = new ChargeReceivedEvent(
                ChargeId.valueOf("CHARGE-001"),
                "ACCOUNT-123",
                "D",
                BigDecimal.valueOf(100.00)
        );
        consumer.subscribe(Collections.singletonList(chargeEventsTopic));
        kafkaPublisherAdapter.publish(event);

        ConsumerRecords<String, ChargeReceivedEvent> records = consumer.poll(Duration.ofSeconds(10));
        assertThat(records).isNotEmpty();
        assertThat(records.count()).isGreaterThanOrEqualTo(1);
        ConsumerRecord<String, ChargeReceivedEvent> record = records.iterator().next();
        ChargeReceivedEvent receivedEvent = record.value();

        assertThat(receivedEvent).isNotNull();
        assertThat(receivedEvent.getChargeId().getValue()).isEqualTo("CHARGE-001");
        assertThat(receivedEvent.getAccountId()).isEqualTo("ACCOUNT-123");
        assertThat(receivedEvent.getType()).isEqualTo("D");
        assertThat(receivedEvent.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
    }

    @Test
    @DisplayName("Should publish event to accounting system topic successfully")
    void shouldPublishToAccountingSystemSuccessfully() {
        ChargeReceivedEvent event = new ChargeReceivedEvent(
                ChargeId.valueOf("CHARGE-002"),
                "ACCOUNT-456",
                "C",
                BigDecimal.valueOf(200.50)
        );
        consumer.subscribe(Collections.singletonList(accountingEventsTopic));
        kafkaPublisherAdapter.publishToAccountingSystem(event);
        ConsumerRecords<String, ChargeReceivedEvent> records = consumer.poll(Duration.ofSeconds(10));
        assertThat(records).isNotEmpty();
        assertThat(records.count()).isGreaterThanOrEqualTo(1);
        ConsumerRecord<String, ChargeReceivedEvent> record = records.iterator().next();
        log.info("📥 Event received from accounting topic: {}", record.value());
//        ChargeReceivedEvent receivedEvent = record.value();
//        assertThat(receivedEvent.getChargeId().getValue()).isEqualTo("CHARGE-002");
//        assertThat(receivedEvent.getAccountId()).isEqualTo("ACCOUNT-456");
//        log.info("✅ Accounting event received and verified successfully");
    }

    @Test
    @DisplayName("Should handle multiple publishes in single test")
    void shouldHandleMultiplePublishesInSingleTest() {
        ChargeReceivedEvent event1 = new ChargeReceivedEvent(
                ChargeId.valueOf("CHARGE-MULTI-1"),
                "ACCOUNT-001",
                "D",
                BigDecimal.valueOf(100.00)
        );
        ChargeReceivedEvent event2 = new ChargeReceivedEvent(
                ChargeId.valueOf("CHARGE-MULTI-2"),
                "ACCOUNT-002",
                "C",
                BigDecimal.valueOf(200.00)
        );
        consumer.subscribe(Collections.singletonList(chargeEventsTopic));
        kafkaPublisherAdapter.publish(event1);
        kafkaPublisherAdapter.publish(event2);
        log.info("📤 Published 2 events");
        ConsumerRecords<String, ChargeReceivedEvent> records = consumer.poll(Duration.ofSeconds(10));
        assertThat(records.count()).isGreaterThanOrEqualTo(2);
        List<String> chargeIds = new ArrayList<>();
        for (ConsumerRecord<String, ChargeReceivedEvent> record : records) {
            chargeIds.add(record.value().getChargeId().getValue());
        }
        assertThat(chargeIds).contains("CHARGE-MULTI-1", "CHARGE-MULTI-2");
        log.info("✅ All 2 events verified successfully");
    }

    @Test
    @DisplayName("Should verify Kafka container is running and accessible")
    void shouldVerifyKafkaContainerIsRunning() {
        assertThat(kafka.isRunning()).isTrue();
        assertThat(kafka.getBootstrapServers()).isNotEmpty();
        log.info("✅ Kafka Container:");
        log.info("   - Status: RUNNING");
        log.info("   - Bootstrap Servers: {}", kafka.getBootstrapServers());
    }
}
