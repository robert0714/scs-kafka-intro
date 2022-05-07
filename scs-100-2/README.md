# SCS-100-2

## Functional Programming (For Alternative of SCS-100 Project)

A simple Example of an Event Driven Flow by the help of **SPRING CLOUD STREAM KAFKA**

##### properties

* java.version: `11`
* spring-cloud.version: `2020.0.5`
* spring-boot.version: `2.5.13`

### Documentation
Please visit [Spring Cloud Stream Kafka (Part 3)](https://tanzu.vmware.com/developer/guides/event-streaming/spring-cloud-stream-kafka-p3/) for Project documentation


![General Flow Diagram](material/kafka-events-intro-1002-1.svg)

The Docker-compose file contains: single kafka and zookeeper. just simply run the following command

```shell
docker-compose up -d
```

or 
- Control Center UI
Runs a [Confluent Control Center](https://docs.confluent.io/platform/current/control-center/index.html) that exposes a UI at `http://localhost:9021/` .

```shell
docker-compose -f ./kafka-cluster.yml -f ./control-center-ui.yml up
```
To stop the brokers and the Control Center UI run the following command:
```shell
docker-compose -f ./kafka-cluster.yml -f ./control-center-ui.yml down
```

> I assume you already have docker setup in your machine.


### Make the project

run the following command line to create you jar file in `target` directory

```shell
mvn clean package
```

Then run the generated jar file in `target` folder, (so make sure you are in the same directory when you run the jar
file or give the full path)

```shell
java -jar scs-100-0.0.1-SNAPSHOT.jar
```

the application starts to listen on port 8080.

**To scale** the application horizontally you can add the following parameter before `-jar` by
adding `--server.port=8081` (basically a different port) as:

```shell
java --server.port=8081 -jar scs-100-0.0.1-SNAPSHOT.jar
```
or
```shell
mvn spring-boot:run -Dserver.port=8081
```

> When you running multiple instances of the same application on a single machine, this path must be unique for each
such instance.
At this point you should have already seen the information about your topics
, [to read more](https://kafka.apache.org/28/documentation/streams/developer-guide/config-streams.html#state-dir)

### Check Application

#### Create Order or Place your Order

you should now be able to place your order by calling the following `curl` command

```shell
# assuming your app is listening on 8080
ORDER_UUID=$(curl --silent -H 'Content-Type: application/json' -d "{\"itemName\":\"book\"}" http://localhost:8080/order | jq -r '.orderUuid') && for i in `seq 1 15`; do sleep 1; echo $(curl --silent "http://localhost:8080/order/status/"$ORDER_UUID); done;
```

Note: make sure you have already installed the `jq`

## Running Multi Instances

Now let’s run the same application multiple times at the same time to simulate the application redundancy. But before
that make sure that the current application is not running.

This project code comes with Nginx as LoadBalancer which has already been configured to distribute the incoming traffic
from port 8080 and route it into 8081 and 8082.

So first let’s start it in different docker-compose from root on this project “scs-100-2” as:

```shell
docker-compose -f nginx/docker-compose.yml up -d
```

Since the port 8080 is already got occupied by nginx we can run the Ordering application as follow in 2 separated
terminal

_Terminal 1:_

```shell
java --server.port=8081 -jar target/scs-100-2-0.0.1-SNAPSHOT.jar
```
or 
```shell
mvn spring-boot:run -Dserver.port=8081
```

_And on Terminal 2:_

```shell
java --server.port=8082 -jar target/scs-100-2-0.0.1-SNAPSHOT.jar
```
or 
```shell
mvn spring-boot:run -Dserver.port=8082
```
![General Flow Diagram](material/kafka-events-intro-1002-3.svg)

_Then run our curl call command again (same as the earlier one)_

```shell
ORDER_UUID=$(curl --silent -H 'Content-Type: application/json' -d "{\"itemName\":\"book\"}" http://localhost:8080/order | jq -r '.orderUuid') && for i in `seq 1 15`; do sleep 1; echo $(curl --silent "http://localhost:8080/order/status/"$ORDER_UUID); done;
```

![General Flow Diagram](material/kafka-events-intro-1002-4.svg)


Please visit [Spring Cloud Stream Kafka (Part 3)](https://tanzu.vmware.com/developer/guides/event-streaming/spring-cloud-stream-kafka-p3/) for Project documentation
## Spring Cloud Stream - functional and reactive
* [Spring Cloud Function](https://spring.io/projects/spring-cloud-function)
* [Cloud Events and Spring - part 1 (Oleg Zhurakousky)](https://spring.io/blog/2020/12/10/cloud-events-and-spring-part-1)
* [Cloud Events and Spring - part 2 (Oleg Zhurakousky)](https://spring.io/blog/2020/12/23/cloud-events-and-spring-part-2)
* [Spring Cloud Stream - demystified and simplified](https://spring.io/blog/2019/10/14/spring-cloud-stream-demystified-and-simplified)
* [Spring Cloud Stream - functional and reactive (Oleg Zhurakousky)](https://spring.io/blog/2019/10/17/spring-cloud-stream-functional-and-reactive)

## Stream Processing with Spring Cloud Stream and Apache Kafka Streams. Part 6 - State Stores and Interactive Queries
* Part 1 - [Programming Model](https://spring.io/blog/2019/12/02/stream-processing-with-spring-cloud-stream-and-apache-kafka-streams-part-1-programming-model)
* Part 2 - [Programming Model Continued](https://spring.io/blog/2019/12/03/stream-processing-with-spring-cloud-stream-and-apache-kafka-streams-part-2-programming-model-continued)
* Part 3 - [Data deserialization and serialization](https://spring.io/blog/2019/12/04/stream-processing-with-spring-cloud-stream-and-apache-kafka-streams-part-3-data-deserialization-and-serialization)
* Part 4 - [Error Handling](https://spring.io/blog/2019/12/05/stream-processing-with-spring-cloud-stream-and-apache-kafka-streams-part-4-error-handling)
* Part 5 - [Application Customizations](https://spring.io/blog/2019/12/06/stream-processing-with-spring-cloud-stream-and-apache-kafka-streams-part-5-application-customizations)

* Part 6 - [State Stores and Interactive Queries](https://spring.io/blog/2019/12/09/stream-processing-with-spring-cloud-stream-and-apache-kafka-streams-part-6-state-stores-and-interactive-queries)

### Using interactive queries to query data from state stores
Kafka Streams lets you interactively query the data in the state store in real time as live stream processing is going on. The binder provides abstractions around this feature to make it easier to work with interactive queries. ``InteractiveQueryService`` is a basic API that the binder provides to work with state store querying. You can usually inject this as a bean into your application and then invoke various API methods from it. Here is an example:
```java
@Autowired
private InteractiveQueryService interactiveQueryService;
...
ReadOnlyKeyValueStore<Object, Object> keyValueStore =
interactiveQueryService.getQueryableStoreType("my-store", QueryableStoreTypes.keyValueStore());
```
Then you can invoke various retrieval methods from the store and iterate through the result. There are various methods that you can invoke from these state stores based on your use case and the type of state store that you are using. Please refer to the Kafka Streams documentation for [interactive queries](https://kafka.apache.org/10/documentation/streams/developer-guide/interactive-queries.html) for these various iteration methods available.