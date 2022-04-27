package com.ehsaniara.scs_kafka_intro.scs1002.json;
 
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import com.ehsaniara.scs_kafka_intro.scs1002.Order; 

public class OrderSerdes implements Serde<Order> {

  @Override
  public Serializer<Order> serializer() {
    return new OrderSerializer();
  }

  @Override
  public Deserializer<Order> deserializer() {
    return new OrderDeserializer();
  }
}
