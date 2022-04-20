package com.ehsaniara.scs_kafka_intro.scs1002;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test; 
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,properties = { "server.port=8081" })
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
public class OrderControllerTest {
	@Autowired
	protected TestRestTemplate restTemplate;

	@Autowired
	private ObjectMapper objectMapper;
	
	@Test
	public void testStatusCheck() throws Exception {
		final String uri = "/order";
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request = new HttpEntity<String>("{\"itemName\":\"book\"}", headers);
		ResponseEntity<String> response = restTemplate.postForEntity(uri, request, String.class);
		log.info("--------------------------------------------------------");
		log.info(response.getBody());
		
		Order order = objectMapper.readValue(response.getBody(), Order.class);
		
		assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
		assertNotNull(order);
		final UUID orderUuid = order.getOrderUuid();
		assertNotNull(orderUuid);
		log.info("--------------------------------------------------------");
		log.info(orderUuid.toString());
		assertEquals(OrderStatus.PENDING, order.getOrderStatus());
		for (int i = 0; i < 16; i++) {
			Thread.sleep(1_000);
			final 	OrderStatus status = getOrderStatus(orderUuid);
			assertNotNull(status);
			log.info("------------     "+i+"     --------------");
			log.info(status.toString());
		}
	}


	protected OrderStatus getOrderStatus(UUID orderUuid) {
		final String uri = "/order/status/" + orderUuid.toString();
		ResponseEntity<OrderStatus> response = restTemplate.getForEntity(uri, OrderStatus.class);
		assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
		final OrderStatus result = response.getBody();
		assertNotNull(result);
		return result;
	}
}
