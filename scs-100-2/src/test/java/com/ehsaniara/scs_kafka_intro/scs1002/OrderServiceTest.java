package com.ehsaniara.scs_kafka_intro.scs1002;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.ValueMapper;
import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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

import kafka.tools.StreamsResetter;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,properties = { "server.port=8081" })
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" }, topics = {  "scs-100-2.inventoryCheck", "scs-100-2.orderProcess",
		"scs-100-2.orderStatus", "scs-100-2.shipped", "scs-102.shipping", "scs-100-2.inventoryCheck",
		"scs-100-2-orderStateStoreProcessor-scs-100-2-order-events-repartition",
		"scs-100-2-orderStateStoreProcessor-scs-100-2-order-events-changelog"  })
public class OrderServiceTest {
	
	@Autowired
	private OrderService orderService ; 
	private UUID orderUuid ; 
	@Test
	@org.junit.jupiter.api.Order(2)
	@Disabled
	public void testStatusCheck() throws InterruptedException { 		
		OrderStatus status = orderService.statusCheck().apply(orderUuid);
 
	}
	public void beforeDestroy() {
		
	}

	@Test
	@org.junit.jupiter.api.Order(1)
	public void testPlaceOrder() throws InterruptedException {  
		
		Order order =new Order();
		order.setItemName("book");
		Order outorder = orderService.placeOrder().apply(order );
		assertNotNull(outorder);
	    orderUuid = outorder.getOrderUuid();
		assertNotNull(orderUuid);
	}
	
 



}
