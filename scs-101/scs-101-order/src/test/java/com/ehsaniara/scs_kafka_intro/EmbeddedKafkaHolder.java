package com.ehsaniara.scs_kafka_intro;

import java.util.Map;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import kafka.common.KafkaException; 
/**
 * https://docs.spring.io/spring-kafka/docs/2.7.13/reference/html/#using-the-same-brokers-for-multiple-test-classes
 * ***/
public final class EmbeddedKafkaHolder {

    private static EmbeddedKafkaBroker embeddedKafka = null;
 

    public static EmbeddedKafkaBroker getEmbeddedKafka() {
        if (embeddedKafka ==null) {
        	 synchronized (EmbeddedKafkaBroker.class) {
                 if (embeddedKafka == null) {
                	 embeddedKafka = new EmbeddedKafkaBroker(1, false)
                	            .brokerListProperty("spring.cloud.stream.kafka.streams.binder.brokers");
                	 try {
                         embeddedKafka.afterPropertiesSet();
                         
                     }
                     catch (Exception e) {
                         throw new KafkaException("Embedded broker failed to start", e);
                     } 
                	 
                 }
             }
            
        }
        return embeddedKafka;
    }

    private EmbeddedKafkaHolder() {
        super();
    }

}