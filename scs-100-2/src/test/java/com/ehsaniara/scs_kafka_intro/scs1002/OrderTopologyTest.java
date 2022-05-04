package com.ehsaniara.scs_kafka_intro.scs1002;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes; 
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Printed;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ehsaniara.scs_kafka_intro.scs1002.json.OrderSerdes;
import com.fasterxml.jackson.databind.ObjectMapper;


//@SpringBootTest( properties = { "server.port=8081" })
//@DirtiesContext
//@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" }, topics = { "scs-100-2.inventoryCheck", "scs-100-2.orderProcess",
//		"scs-100-2.orderStatus", "scs-100-2.shipped", "scs-102.shipping", "scs-100-2.inventoryCheck",
//		"scs-100-2-orderStateStoreProcessor-scs-100-2-order-events-repartition",
//		"scs-100-2-orderStateStoreProcessor-scs-100-2-order-events-changelog"  })
public class OrderTopologyTest {
//	@Autowired
//	private OrderService service ;
	
	
	
	/***
	 * custom value serdes reference: <br/> 
	 * https://github.com/robert0714/mastering-kafka-streams-and-ksqldb-2021/blob/master/chapter-03/crypto-sentiment/src/main/java/com/magicalpipelines/serialization/Tweet.java
	 * <br/>
	 * https://github.com/robert0714/mastering-kafka-streams-and-ksqldb-2021/blob/master/chapter-03/crypto-sentiment/src/main/java/com/magicalpipelines/CryptoTopology.java
	 * **/
	@Test
	@Disabled
	public void testKStreamKTableStringFunction() {
		
	    // the builder is used to construct the topology
	    StreamsBuilder builder = new StreamsBuilder();	    
	    
	    // start streaming Order using our custom value serdes. Note: regarding
	    // the key serdes (Serdes.UUID(), if could also use Serdes.Void()
	    // if we always expect our keys to be null
		KStream<UUID, Order> stream = 
				builder.stream("input-topic", Consumed.with(Serdes.UUID(), new OrderSerdes()));		
		stream.print(Printed.<UUID, Order>toSysOut().withLabel("order-stream"));
		
		 // filter out CANCELED
		stream.filterNot( (key,order) ->{
			return order.getOrderStatus().equals(OrderStatus.CANCELED);//return type is boolean
		});

		final 	KTable<UUID, String> result = OrderTopology.kStreamKTableStringFunction.apply(stream);
		 
		assertThat(result).isNotNull();
	}
	public Serde<Order> orderJsonSerde() {
        return new JsonSerde<>(Order.class, new ObjectMapper());
    }
	@Test
	public void testKStreamKTableStringFunction_2() {
		
	    // the builder is used to construct the topology
	    StreamsBuilder builder = new StreamsBuilder();	    
	    
	    // start streaming Order using our custom value serdes. Note: regarding
	    // the key serdes (Serdes.UUID(), if could also use Serdes.Void()
	    // if we always expect our keys to be null
		KStream<UUID, Order> stream = 
				builder.stream("input-topic", Consumed.with(Serdes.UUID(), orderJsonSerde()));		
		stream.print(Printed.<UUID, Order>toSysOut().withLabel("order-stream"));
		
		 // filter out CANCELED
		KStream<UUID, Order> filtered = stream.filterNot( (key,order) ->{
			return order.getOrderStatus().equals(OrderStatus.CANCELED);//return type is boolean
		});

		final 	KTable<UUID, String> result = OrderTopology.kStreamKTableStringFunction.apply(stream);
		 
		assertThat(result).isNotNull();
		
		// branch based on order status
		final 	KStream<UUID, Order>[] branches = filtered.branch(OrderTopology.isOrderMadePredicate, OrderTopology.isShippedPredicate);
		
		// pending status
		KStream<UUID, Order> pendingStream = branches[0];
		pendingStream.print(Printed.<UUID, Order>toSysOut().withLabel("order-pending"));

		// shippedg status
		KStream<UUID, Order> shippedStream = branches[1];
		shippedStream.print(Printed.<UUID, Order>toSysOut().withLabel("order-shipped"));		
	}
	
	final  String orderTopic = "scs-100-2.orderProcess";
	@Test
	public void testOrderJsonSerdeFactoryFunction() throws InterruptedException {
		String bootstrapServer="localhost:9092";
		
		final Order	orderIn = mock(Order.class);
		
		when(orderIn.getItemName()).thenReturn("books");	
		
		//create an order
        var order = Order.builder()//
                .itemName(orderIn.getItemName())//
                .orderUuid(UUID.randomUUID())//
                .orderStatus(OrderStatus.PENDING)//
                .build();
        
	    final	DefaultKafkaProducerFactory<UUID, Order> factory
	    
	    = OrderTopology.orderJsonSerdeFactoryFunction.apply( orderJsonSerde().serializer(), bootstrapServer);
	    
		assertThat(factory).isNotNull();
	    
	    //producer test sending
	    new KafkaTemplate<>(factory) {{           
			setDefaultTopic(orderTopic );
            sendDefault(order.getOrderUuid(), order);
        }};
        final  UUID uuid = order.getOrderUuid();
        assertThat(uuid).isNotNull();
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
         
        
	}
}
