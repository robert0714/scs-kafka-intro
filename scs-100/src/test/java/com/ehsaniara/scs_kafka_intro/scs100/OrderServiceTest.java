package com.ehsaniara.scs_kafka_intro.scs100;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,properties = { "pdc.check.api.mode=SERVLET" })
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
public class OrderServiceTest {

	@Autowired
	private OrderService service ;
	
    
	@BeforeEach
	protected void setUp() throws Exception {
	}

	@Test
	public void testStatusCheck() {
		Order order = new Order();
		order.setItemName("book");
		order = service.placeOrder(order);
		final UUID orderUuid = order.getOrderUuid();
		assertNotNull(orderUuid);
		OrderStatus status = service.statusCheck(orderUuid);
		assertEquals(OrderStatus.PENDING, status);
	}

	@Test
	public void testPlaceOrder() {
		Order order = new Order();
		order.setItemName("book");
		order = service.placeOrder(order);
		final UUID orderUuid = order.getOrderUuid();
		assertNotNull(orderUuid); 
	}

	@Test
	public void testCheckInventory()  {
		final Order order = new Order();
		order.setItemName("book");
		final Order orderOut = service.placeOrder(order);
		final UUID orderUuid = orderOut.getOrderUuid();
		assertNotNull(orderUuid);
		OrderStatus status = service.statusCheck(orderUuid);
		assertEquals(OrderStatus.PENDING, status);

		OrderFailedException exception = Assertions.assertThrows(OrderFailedException.class, () -> {
			service.checkInventory(orderOut);
		},String.format("insufficient inventory for order: %s", orderOut.getOrderUuid()));
		Assertions.assertEquals(String.format("insufficient inventory for order: %s", orderOut.getOrderUuid()), exception.getMessage());

		status = service.statusCheck(orderUuid);

		assertEquals(OrderStatus.OUT_OF_STOCK, status);
	}

	@Test
	public void testShipIt() {
		Order order = new Order();
		order.setItemName("book");
		order = service.placeOrder(order);
		final UUID orderUuid = order.getOrderUuid();
		assertNotNull(orderUuid);
		OrderStatus status = service.statusCheck(orderUuid);
		assertEquals(OrderStatus.PENDING, status);
		service.shipIt(order);
		status = service.statusCheck(orderUuid);
		assertEquals(OrderStatus.SHIPPED, status);
		service.cancelOrder(order);
	}

	@Test
	public void testCancelOrder() {
		Order order = new Order();
		order.setItemName("book");
		order = service.placeOrder(order);
		final UUID orderUuid = order.getOrderUuid();
		assertNotNull(orderUuid);
		OrderStatus status = service.statusCheck(orderUuid);
		assertEquals(OrderStatus.PENDING, status);
		service.cancelOrder(order);
		status = service.statusCheck(orderUuid);
		assertEquals(OrderStatus.CANCELED, status);
		service.cancelOrder(order);
	}


}
