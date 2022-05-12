package com.ehsaniara.scs_kafka_intro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment; 
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;

import com.ehsaniara.scs_kafka_intro.module.Order;
import com.ehsaniara.scs_kafka_intro.module.OrderStatus; 
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
@Slf4j
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,properties = { "SPRING_PROFILES_ACTIVE=kube" ,"api.mode=REACTIVE","spring.cloud.stream.kafka.streams.binder.brokers: localhost:9092"})
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
public class OrderHandlerTest {
	private  WebTestClient client ;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private OrderRoutes config;

	@Autowired
	private OrderHandler handler;
	@BeforeEach
	protected void setUp() throws Exception {
		client = WebTestClient.bindToRouterFunction(config.routes(handler)).build();
	}

	@Test
	public void testPlaceOrder() throws Exception {
		String uri = "/order" ;
		
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
 
