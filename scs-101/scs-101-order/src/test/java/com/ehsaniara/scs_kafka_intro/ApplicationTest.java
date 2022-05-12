package com.ehsaniara.scs_kafka_intro;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
 

@SpringBootTest( properties = {  "spring.cloud.stream.kafka.streams.binder.brokers: localhost:9092" })
@DirtiesContext
@EmbeddedKafka( brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" },topics = {"scs-101.orderStatus","scs-101.orderProcess" })
public class ApplicationTest {
    @Autowired
    public KafkaTemplate<String, String> template;
 
	@BeforeEach
	protected void setUp() throws Exception {
		 
	}
	 

	@Test
	@Disabled
	void contextLoads() {
	}
}
