package com.ehsaniara.scs_kafka_intro.scs099;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.hamcrest.Matcher;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest; 
import org.springframework.integration.support.MessageBuilder;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;

 
 


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
public class ApplicationTest {

	@ClassRule
	public static EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(1, true ,"scs-099.order");


	@Value("${spring.cloud.stream.bindings.order-in.destination}")
	private String inputTopic;

	@Value("${spring.cloud.stream.bindings.order-out.destination}")
	private String outputTopic;

	@BeforeEach
	protected void setUp() throws Exception {
		embeddedKafka.kafkaPorts(9092);
		String brokerinfo =  embeddedKafka.getEmbeddedKafka().getBrokersAsString() ;
		System.setProperty("spring.cloud.stream.kafka.binder.brokers",brokerinfo);
	}
	
	@Test
	public void testSendReceive() throws Exception {
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
