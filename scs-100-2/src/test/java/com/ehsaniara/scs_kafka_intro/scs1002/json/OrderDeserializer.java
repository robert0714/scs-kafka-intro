package com.ehsaniara.scs_kafka_intro.scs1002.json;

import com.ehsaniara.scs_kafka_intro.scs1002.Order; 
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder; 
import java.nio.charset.StandardCharsets;
import org.apache.kafka.common.serialization.Deserializer;

public class OrderDeserializer implements Deserializer<Order> {
  private Gson gson =
      new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

  @Override
  public Order deserialize(String topic, byte[] bytes) {
    if (bytes == null) return null;
    return gson.fromJson(new String(bytes, StandardCharsets.UTF_8), Order.class);
  }
}
