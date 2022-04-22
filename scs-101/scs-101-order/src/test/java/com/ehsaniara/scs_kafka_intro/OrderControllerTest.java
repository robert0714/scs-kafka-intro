package com.ehsaniara.scs_kafka_intro;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;
import org.springframework.web.server.WebHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test; 
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.ehsaniara.scs_kafka_intro.module.Order;
import com.ehsaniara.scs_kafka_intro.module.OrderStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,properties = { "SPRING_PROFILES_ACTIVE=kube" ,"spring.cloud.stream.kafka.streams.binder.brokers: localhost:9092"})
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
public class OrderControllerTest {
	@Autowired
	protected TestRestTemplate restTemplate;

	@Autowired
	private ObjectMapper objectMapper;
	
	private  WebTestClient client ;
	
	@Autowired
	private OrderController controller;

	@BeforeEach
	void setup() throws Exception { 
		this.client = WebTestClient.bindToController(controller).build();
	}
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
	protected OrderStatus getOrderStatus(UUID orderUuid) throws  Exception {
		final String uri = "/order/status/" + orderUuid.toString();
		ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
		assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
		log.info("--------------------------------------------------------");
		log.info(response.getBody());
		
		final OrderStatus result = objectMapper.readValue(response.getBody(), OrderStatus.class);
		assertNotNull(result);
		return result;
	}
	@Test
	public void testStatusCheck_wbflux() throws Exception {
		final String uri = "/order";
		
		client.post().uri(uri)
		.contentType(MediaType.APPLICATION_JSON)
		.body(Mono.just("{\"itemName\":\"book\"}"), String.class)
		.exchange()
		.expectStatus()
		.isOk();
		
		EntityExchangeResult<byte[]> response = client
		.post()
		.uri(uri)
		.contentType(MediaType.APPLICATION_JSON)
		.body(Mono.just("{\"itemName\":\"book\"}"),String.class)
		.exchange().expectStatus().is2xxSuccessful()
		.expectBody().returnResult();
		
		byte[] byteBody = response.getResponseBody();
		String body = new String(byteBody);
		Order order = objectMapper.readValue(body, Order.class);
		
		
		assertNotNull(order);
		final UUID orderUuid = order.getOrderUuid();
		assertNotNull(orderUuid);
		log.info("--------------------------------------------------------");
		log.info(orderUuid.toString());
		assertEquals(OrderStatus.PENDING, order.getOrderStatus());
		for (int i = 0; i < 16; i++) {
			Thread.sleep(1_000);
			final 	OrderStatus status = getOrderStatus_wbflux(orderUuid);
			assertNotNull(status);
			log.info("------------     "+i+"     --------------");
			log.info(status.toString());
		}
	}

	protected OrderStatus getOrderStatus_wbflux(UUID orderUuid) throws Exception {
		final String uri = "/order/status/" + orderUuid.toString();
		ResponseSpec response = client.get().uri(uri).exchange();

		response.expectStatus().is2xxSuccessful();
		log.info("--------------------------------------------------------");
		FluxExchangeResult<OrderStatus> content = response.returnResult(OrderStatus.class);
		;

		final OrderStatus result = content.getResponseBody().blockLast();
		assertNotNull(result);
		return result;
	}
}
