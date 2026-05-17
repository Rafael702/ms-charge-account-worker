package com.itau.chargeaccount.infrastructure.config;

import com.itau.chargeaccount.domain.event.DomainEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Configuration for the Charge Account Worker.
 *
 * Configures:
 * - Kafka topics (charge.events, account.status.response, accounting.events, DLQ)
 * - Producer factory with serialization settings
 * - Kafka template for publishing messages
 */
@Configuration
@EnableKafka
public class KafkaConfiguration {

    @Value("${app.kafka.topics.charge-events}")
    private String chargeEventsTopic;

    @Value("${app.kafka.topics.account-status}")
    private String accountStatusTopic;

    @Value("${app.kafka.topics.accounting-events}")
    private String accountingEventsTopic;

    @Value("${app.kafka.topics.dlq}")
    private String dlqTopic;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Kafka topic for charge received events.
     * Partitions: 10 for high throughput
     * Replication factor: 1 (can be changed for production)
     */
    @Bean
    public NewTopic chargeEventsTopic() {
        return TopicBuilder.name(chargeEventsTopic)
                .partitions(10)
                .replicas(1)
                .config("retention.ms", "86400000")  // 24 hours
                .config("compression.type", "snappy")
                .build();
    }

    /**
     * Kafka topic for account validation responses.
     */
    @Bean
    public NewTopic accountStatusTopic() {
        return TopicBuilder.name(accountStatusTopic)
                .partitions(10)
                .replicas(1)
                .config("retention.ms", "86400000")
                .config("compression.type", "snappy")
                .build();
    }

    /**
     * Kafka topic for processed charges sent to accounting system.
     */
    @Bean
    public NewTopic accountingEventsTopic() {
        return TopicBuilder.name(accountingEventsTopic)
                .partitions(10)
                .replicas(1)
                .config("retention.ms", "86400000")
                .config("compression.type", "snappy")
                .build();
    }

    /**
     * Dead Letter Queue topic for failed events.
     */
    @Bean
    public NewTopic dlqTopic() {
        return TopicBuilder.name(dlqTopic)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "604800000")  // 7 days
                .build();
    }

    /**
     * Producer factory configuration for Kafka events.
     * Uses JSON serialization for DomainEvent objects.
     */
    @Bean
    public ProducerFactory<String, DomainEvent> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Kafka template for sending messages to topics.
     * Used by KafkaPublisherAdapter.
     */
    @Bean
    public KafkaTemplate<String, DomainEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}

