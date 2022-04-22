package com.ehsaniara.scs_kafka_intro.scs1002;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
 
import com.fasterxml.jackson.databind.ObjectMapper;
 

@SpringBootTest( properties = { "server.port=8081" })
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
public class ApplicationTest {

	@ClassRule
	public static EmbeddedKafkaRule embeddedKafka ;
	
	@Autowired
	private EmbeddedKafkaBroker embeddedKafkaBroker;
	 
	@Autowired
	private OrderService service ;
	
	private ObjectMapper om ;
	
//	private final String TOPIC_NAME_01 = "scs-102.shipping";
    @Value("${spring.cloud.stream.bindings.orderStateStoreProcessor-in-0.destination}")
    private String orderTopic;

	@BeforeEach
	protected void setUp() throws Exception {
		embeddedKafka = new EmbeddedKafkaRule(1, true, "scs-100-2.inventoryCheck", "scs-100-2.orderProcess",
				"scs-100-2.orderStatus", "scs-100-2.shipped", "scs-102.shipping", "scs-100-2.inventoryCheck",
				"scs-100-2-orderStateStoreProcessor-scs-100-2-order-events-repartition",
				"scs-100-2-orderStateStoreProcessor-scs-100-2-order-events-changelog");
//		embeddedKafka.zkPort(embeddedKafkaBroker.getZkPort());
		embeddedKafka.kafkaPorts(9092);
		String brokerinfo =  embeddedKafka.getEmbeddedKafka().getBrokersAsString() ;
		System.setProperty("spring.cloud.stream.kafka.binder.brokers",brokerinfo);
		this.om = new ObjectMapper();
	}
	
	@Test
	public void testSendReceive() throws Exception {
		Map<String, Object> senderProps = KafkaTestUtils.producerProps(embeddedKafka.getEmbeddedKafka());
		senderProps.put("key.serializer", ByteArraySerializer.class);
		senderProps.put("value.serializer", ByteArraySerializer.class);
		
		final DefaultKafkaProducerFactory<byte[], byte[]> pf = new DefaultKafkaProducerFactory<>(senderProps);
		
		KafkaTemplate<byte[], byte[]> template = new KafkaTemplate<>(pf, true);
		template.setDefaultTopic(orderTopic);
		
		final UUID uuid = UUID.randomUUID() ;
		
		var order = Order.builder()//
                .itemName("unitTest")//
                .orderUuid(uuid)//
                .orderStatus(OrderStatus.PENDING)//
                .build();
		template.sendDefault(this.om.writeValueAsBytes(order));
		
		Thread.sleep(5000L);
		
		OrderStatus status = service.statusCheck().apply(uuid);		
		System.out.println("-----------------------------------------------------------");
		System.out.println(status.name());
		assertThat(status).isEqualTo(OrderStatus.PENDING);
		
		pf.destroy();
	}
}
