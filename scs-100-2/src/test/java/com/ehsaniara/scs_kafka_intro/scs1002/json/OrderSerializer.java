package com.ehsaniara.scs_kafka_intro.scs1002.json;

import com.ehsaniara.scs_kafka_intro.scs1002.Order; 
import com.google.gson.Gson; 
import java.nio.charset.StandardCharsets;
import org.apache.kafka.common.serialization.Serializer;

class OrderSerializer implements Serializer<Order> {
  private Gson gson = new Gson();

  @Override
  public byte[] serialize(String topic, Order order) {
    if (order == null) return null;
    return gson.toJson(order).getBytes(StandardCharsets.UTF_8);
  }
}
