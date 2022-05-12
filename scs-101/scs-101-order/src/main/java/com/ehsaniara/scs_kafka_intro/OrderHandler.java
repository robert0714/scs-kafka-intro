package com.ehsaniara.scs_kafka_intro;

import static java.util.logging.Level.FINE;
 
import java.util.UUID;
 
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.ehsaniara.scs_kafka_intro.module.Order;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class OrderHandler {
	private final OrderService orderService;

	public OrderHandler(final OrderService orderService) {
		this.orderService = orderService;
	}

	public Mono<ServerResponse> placeOrder(final ServerRequest request) {
		final Mono<Order> requestMono = request.bodyToMono(Order.class);
		final Mono<Order> mono = requestMono.map(order -> orderService.placeOrder().apply(order));
		return ServerResponse.ok().body(mono, Order.class).log(log.getName(), FINE);
	}

	public Mono<ServerResponse> statusCheck(final ServerRequest request) {
		final String orderUuid = request.pathVariable("orderUuid");
		return ServerResponse.ok().bodyValue(orderService.statusCheck().apply(UUID.fromString(orderUuid)))
				.log(log.getName(), FINE);
	}

}
