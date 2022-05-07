# Introduction to Spring Cloud Stream Kafka
Introduction to Spring Cloud Stream Kafka

## Spring Cloud Stream Kafka (Part1)

[Documentation (Part 1)](https://tanzu.vmware.com/developer/guides/event-streaming/spring-cloud-stream-kafka-p1/)

[Project Source Code](./scs-099)

## Spring Cloud Stream Kafka (Part2) Binders

Order Example

Key features:
- spring boot (2.4.13)
- Spring cloud Stream kafka (Hoxton.SR12)
- Binders `@EnableBinding`

[Documentation (Part 2) Binders](https://tanzu.vmware.com/developer/guides/event-streaming/spring-cloud-stream-kafka-p2/)

[Project Source Code](./scs-100)

![General Flow Diagram](./scs-100/material/kafka-events-intro-100.svg)

> **⚠ NOTE:**   Starting with ``spring-cloud-stream-binder-kafka/spring-cloud-starter-stream-kafka 3.0``, when ``spring.cloud.stream.binding.<name>.consumer.batch-mode`` is set to ``true``, all of the records received by polling the Kafka Consumer will be presented as a List<?> to the listener method. Otherwise, the method will be called with one record at a time. The size of the batch is controlled by Kafka consumer properties max.poll.records, fetch.min.bytes, fetch.max.wait.ms; refer to the Kafka documentation for more information.
> 
> Bear in mind that ``batch mode is not supported with @StreamListener`` - it only works with the newer ``functional programming model``.
>  reference: https://docs.spring.io/spring-cloud-stream-binder-kafka/docs/current/reference/html/spring-cloud-stream-binder-kafka.html#pause-resume


## Spring Cloud Stream - functional and reactive
* [Spring Cloud Function](https://spring.io/projects/spring-cloud-function)
* [Cloud Events and Spring - part 1 (Oleg Zhurakousky)](https://spring.io/blog/2020/12/10/cloud-events-and-spring-part-1)
* [Cloud Events and Spring - part 2 (Oleg Zhurakousky)](https://spring.io/blog/2020/12/23/cloud-events-and-spring-part-2)
* [Spring Cloud Stream - demystified and simplified](https://spring.io/blog/2019/10/14/spring-cloud-stream-demystified-and-simplified)
* [Spring Cloud Stream - functional and reactive (Oleg Zhurakousky)](https://spring.io/blog/2019/10/17/spring-cloud-stream-functional-and-reactive)

## Spring Cloud Stream Kafka (Part3) Functional Programming

Order Example 

Key features:
- spring boot (2.5.13)
- Spring cloud Stream kafka (2020.0.5)
- StageManger 
- Functional interface

[Documentation (Part 3) Functional Programming](https://tanzu.vmware.com/developer/guides/event-streaming/spring-cloud-stream-kafka-p3/)

[Project Source Code](./scs-100-2)

![General Flow Diagram](./scs-100-2/material/kafka-events-intro-1002-2.svg)

## Spring Cloud Stream Kafka (Part4) Kubernetes & Microservices 

[Project Source Code](./scs-101)


### Testing Spring Cloud Stream Applications 
[Testing Spring Cloud Stream Applications - Part 2](https://spring.io/blog/2020/12/15/testing-spring-cloud-stream-applications-part-2)
[Project Source Code](https://github.com/spring-cloud/spring-cloud-stream-samples/tree/main/function-based-stream-app-samples/couchbase-stream-applications)

#### Relevant articles
- [Intro to Apache Kafka with Spring](https://www.baeldung.com/spring-kafka)
- [Testing Kafka and Spring Boot](https://www.baeldung.com/spring-boot-kafka-testing)
- [Monitor the Consumer Lag in Apache Kafka](https://www.baeldung.com/java-kafka-consumer-lag)
- [Send Large Messages With Kafka](https://www.baeldung.com/java-kafka-send-large-message)
- [Configuring Kafka SSL Using Spring Boot](https://www.baeldung.com/spring-boot-kafka-ssl)
- [Kafka Streams With Spring Boot](https://www.baeldung.com/spring-boot-kafka-streams)
- [Baeldung's Relevant Articles  Source Code](https://github.com/eugenp/tutorials/tree/master/spring-kafka)

### Official Sample
- [Official Sample  Source Code](https://github.com/spring-cloud/spring-cloud-stream-samples)


#### Function Based Stream Application Samples
- [Thumbnail Demo](https://github.com/spring-cloud/spring-cloud-stream-samples/tree/main/function-based-stream-app-samples/image-thumbnail-samples)


#### Processing of record batches
- [kafka-batch-sample](https://github.com/spring-cloud/spring-cloud-stream-samples/tree/main/kafka-batch-sample)

#### Spring Cloud Stream Kafka and native encoding
In this *Spring Cloud Stream* sample, we demonstrate native encoding with Kafka and functions.Spring Cloud Stream will skip the regular message conversion and on the outbound and let Kafka natively perform serialization.

https://github.com/spring-cloud/spring-cloud-stream-samples/tree/main/kafka-native-serialization

#### kafka-streams-samples

https://github.com/spring-cloud/spring-cloud-stream-samples/tree/main/kafka-streams-samples


#### Tools
https://github.com/spring-cloud/spring-cloud-stream-samples/tree/main/tools/kafka/docker-compose

#### Spring Cloud Stream Testing with Embedded Kafka Broker Sample
https://github.com/spring-cloud/spring-cloud-stream-samples/tree/main/testing-samples/test-embedded-kafka


#### How to test Spring Cloud Stream applications ?
This project contains a set of tests for simple Spring Cloud Stream applications to demonstrate what, how and when we can use to test this kind of microservices.  
https://github.com/spring-cloud/spring-cloud-stream-samples/tree/main/testing-samples/testing-demo


#### Spring Cloud Stream Sample Transactional Application

https://github.com/spring-cloud/spring-cloud-stream-samples/tree/main/transaction-kafka-samples
