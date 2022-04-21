package com.ehsaniara.scs_kafka_intro.scs099;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Map;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
public class PobSubServiceTest {

	@ClassRule
	public static EmbeddedKafkaRule embeddedKafka ;
	@Autowired
	private EmbeddedKafkaBroker embeddedKafkaBroker;

	@Value("${spring.cloud.stream.bindings.order-in.destination}")
	private String inputTopic;

	@Value("${spring.cloud.stream.bindings.order-out.destination}")
	private String outputTopic;
	
	@BeforeEach
	protected void setUp() throws Exception {
		embeddedKafka = new EmbeddedKafkaRule(1, true ,"scs-099.order");
		embeddedKafka.zkPort(embeddedKafkaBroker.getZkPort());
		embeddedKafka.kafkaPorts(9092);
		String brokerinfo =  embeddedKafka.getEmbeddedKafka().getBrokersAsString() ;
		System.setProperty("spring.cloud.stream.kafka.binder.brokers",brokerinfo);
	}
	@AfterEach
	protected void down() throws Exception {
//		embeddedKafkaBroker.destroy();
	}

	@Test
	public void testProducer() {
		Map<String, Object> senderProps = KafkaTestUtils.producerProps(embeddedKafka.getEmbeddedKafka());
		senderProps.put("key.serializer", ByteArraySerializer.class);
		senderProps.put("value.serializer", ByteArraySerializer.class);
		DefaultKafkaProducerFactory<byte[], byte[]> pf = new DefaultKafkaProducerFactory<>(senderProps);
		KafkaTemplate<byte[], byte[]> template = new KafkaTemplate<>(pf, true);
		template.setDefaultTopic(inputTopic);
		template.sendDefault("This is testProducer".getBytes());

		Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "false", embeddedKafka.getEmbeddedKafka());
		consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		consumerProps.put("key.deserializer", ByteArrayDeserializer.class);
		consumerProps.put("value.deserializer", ByteArrayDeserializer.class);
		DefaultKafkaConsumerFactory<byte[], byte[]> cf = new DefaultKafkaConsumerFactory<>(consumerProps);

		Consumer<byte[], byte[]> consumer = cf.createConsumer();
		consumer.subscribe(Collections.singleton(this.outputTopic));
		ConsumerRecords<byte[], byte[]> records = consumer.poll(1_000);
		consumer.commitSync();

		assertThat(records.count()).isGreaterThan(0);
		assertThat(new String(records.iterator().next().value())).isEqualTo("This is testProducer");
		consumer.close();
		pf.destroy();
	}

	@Test
//	@Disabled
	public void testConsumer() {
		Map<String, Object> senderProps = KafkaTestUtils.producerProps(embeddedKafka.getEmbeddedKafka());
		senderProps.put("key.serializer", ByteArraySerializer.class);
		senderProps.put("value.serializer", ByteArraySerializer.class);
		DefaultKafkaProducerFactory<byte[], byte[]> pf = new DefaultKafkaProducerFactory<>(senderProps);


		Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "false", embeddedKafka.getEmbeddedKafka());
		consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		consumerProps.put("key.deserializer", ByteArrayDeserializer.class);
		consumerProps.put("value.deserializer", ByteArrayDeserializer.class);
		DefaultKafkaConsumerFactory<byte[], byte[]> cf = new DefaultKafkaConsumerFactory<>(consumerProps);

		Consumer<byte[], byte[]> consumer = cf.createConsumer();
		consumer.subscribe(Collections.singleton(this.outputTopic));
		ConsumerRecords<byte[], byte[]> records = consumer.poll(5_000);
		consumer.commitSync();

		assertThat(records.count()).isGreaterThan(1);
		assertThat(new String(records.iterator().next().value())).isEqualTo("TestString of 0 - 0 (port is 8080)");
		consumer.close();
		pf.destroy();
	   
	}

}
