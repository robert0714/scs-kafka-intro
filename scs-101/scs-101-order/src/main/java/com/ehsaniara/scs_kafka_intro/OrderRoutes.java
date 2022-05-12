package com.ehsaniara.scs_kafka_intro;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
 
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse; 
 
@Configuration
@ConditionalOnProperty(prefix = "api", name = "mode", havingValue = "REACTIVE")
public class OrderRoutes {
	private static final String MAIN_REQUEST_MAPPING = "/order";
	
	private static final String PLACE_ORDER= "";
	
	private static final String STATUS_CHECK= "/status/{orderUuid}";
	 
	@Bean
	public OrderHandler orderHandler(final OrderService orderService) {
		return new OrderHandler(orderService);
	}
	@Bean
	public RouterFunction<ServerResponse> routes(final OrderHandler handler) {
		return route().path(MAIN_REQUEST_MAPPING,
				builder -> builder.POST(PLACE_ORDER, handler::placeOrder)
						.GET(STATUS_CHECK, handler::statusCheck) )
				.build();		 
	}
	 
}
