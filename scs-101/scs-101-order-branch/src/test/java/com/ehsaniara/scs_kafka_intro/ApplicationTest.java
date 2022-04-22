package com.ehsaniara.scs_kafka_intro;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import com.ehsaniara.scs_kafka_intro.module.Order;
 

@SpringBootTest( properties = { "SPRING_PROFILES_ACTIVE=kube","spring.cloud.stream.kafka.streams.binder.brokers: localhost:9092" })
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
public class ApplicationTest {
    @Autowired
    public KafkaTemplate<String, Order> template;

 

	@Test
	void contextLoads() {
//		template.send(topic, payload);
	}
}