package com.ehsaniara.scs_kafka_intro;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity; 
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka; 
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.awaitility.Durations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;

import com.ehsaniara.scs_kafka_intro.module.Order;
import com.ehsaniara.scs_kafka_intro.module.OrderStatus; 
import com.fasterxml.jackson.databind.ObjectMapper; 

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when; 
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;


import static org.awaitility.Awaitility.await;

@Slf4j
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,properties = { 
		"api.mode=SERVLET","server.port=8080","spring.cloud.stream.kafka.streams.binder.brokers: localhost:9092"} )
@DirtiesContext
@EmbeddedKafka(brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" },topics = {"scs-101.orderStatus","scs-101.orderProcess" })
@Disabled
public class OrderControllerTest {
	@Autowired
	protected TestRestTemplate restTemplate;
 
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private  WebTestClient client ;
	
	@Autowired
	private OrderController controller;
	
	@Autowired
	private   InteractiveQueryService interactiveQueryService;
	
	@Autowired 
	private   OrderService orderService;
	
	@BeforeEach
	protected void setUp() throws Exception {
		this.client = WebTestClient.bindToController(controller).build();
		
//		final ReadOnlyKeyValueStore<Object, Object> store =
//                interactiveQueryService.getQueryableStore(Application.STATE_STORE_NAME, QueryableStoreTypes.keyValueStore());		
//		InteractiveQueryService iqsMock =	Mockito.mock(InteractiveQueryService.class);		
//		final HostInfo hostInfo =  HostInfo.buildFromEndpoint("localhost:8080");		
//		when(iqsMock.getQueryableStore(Application.STATE_STORE_NAME, QueryableStoreTypes.keyValueStore())).thenReturn(store);
//		when(iqsMock.getHostInfo(Application.STATE_STORE_NAME, Mockito.anyObject( ),  Mockito.anyObject()))
//				.thenReturn(hostInfo);
//		when(iqsMock.getCurrentHostInfo()).thenReturn(hostInfo);		
//		ReflectionTestUtils.setField( orderService, "interactiveQueryService",iqsMock);
	}
	@AfterEach
	protected void tearDown() throws Exception { 
//		ReflectionTestUtils.setField( orderService, "interactiveQueryService",interactiveQueryService);
		
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
			await().atMost(Durations.TEN_SECONDS).untilAsserted(() -> {	
				assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
				assertNotNull(order);
			});
			final UUID orderUuid = order.getOrderUuid();
			assertNotNull(orderUuid);
			log.info("--------------------------------------------------------");
			log.info(orderUuid.toString());
			assertEquals(OrderStatus.PENDING, order.getOrderStatus());
			for (int i = 0; i < 2; i++) {
				log.info("------------     "+i+"     --------------");
				final 	OrderStatus status = getOrderStatus(orderUuid);
				await().atMost(Durations.TEN_SECONDS).untilAsserted(() -> {	
					assertNotNull(status);
					log.info(status.toString());
				});
			}

		
	}
	@Deprecated
	protected OrderStatus getOrderStatus(UUID orderUuid) throws  Exception {
		final String uri = "/order/status/" + orderUuid.toString();
		ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
		await().atMost(Durations.TEN_SECONDS).untilAsserted(() -> {
			assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
		});
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
			log.info("------------     "+i+"     --------------");
			await().atMost(Durations.TEN_SECONDS).untilAsserted(() -> {
				final OrderStatus status = getOrderStatus_wbflux(orderUuid);
				assertNotNull(status);

				log.info(status.toString());
			});
		}
	}

	protected OrderStatus getOrderStatus_wbflux(UUID orderUuid) throws Exception {
		final String uri = "/order/status/" + orderUuid.toString();
		ResponseSpec response = client.get().uri(uri).exchange();
		await().atMost(Durations.TEN_SECONDS).untilAsserted(() -> {	
		        response.expectStatus().is2xxSuccessful();
		});
		log.info("--------------------------------------------------------");
		FluxExchangeResult<OrderStatus> content = response.returnResult(OrderStatus.class);
		;

		final OrderStatus result = content.getResponseBody().blockLast();
		await().atMost(Durations.TEN_SECONDS).untilAsserted(() -> {	
			assertNotNull(result);
		});
		return result;
	}
}
