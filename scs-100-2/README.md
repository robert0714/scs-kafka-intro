# SCS-100-2

## Functional Programming (For Alternative of SCS-100 Project)

A simple Example of an Event Driven Flow by the help of **SPRING CLOUD STREAM KAFKA**

##### properties

* java.version: `11`
* spring-cloud.version: `2020.0.3` 
* spring-boot.version: `2.5.2`

![General Flow Diagram](material/kafka-events-intro-1002-1.svg)

The Docker-compose file contains: single kafka and zookeeper. just simply run the following command

```shell
docker-compose up -d
```

_Note: I assume you already have docker setup in your machine._

### Make the project

run the following command line to create you jar file in `target` directory

```shell
mvn clean package
```

Then run the generated jar file in `target` folder, (so make sure you are in the same directory when you run the jar file
or give the full path)

```shell
java -jar scs-100-0.0.1-SNAPSHOT.jar
```

the application starts to listen on port 8080. 

**To scale** the application horizontally you can add the following parameter before `-jar` by adding `-Dserver.port=8081` (basically a different port) as:

```shell
java -Dserver.port=8081 -jar scs-100-0.0.1-SNAPSHOT.jar
```
_Note: "When running multiple instances of the same application on a single machine, this path must be unique for each such instance._
At this point you should have already seen the information about your topics", [to read more](https://kafka.apache.org/28/documentation/streams/developer-guide/config-streams.html#state-dir)

### Check Application

#### Create Order or Place your Order
you should now be able to place your order by calling the following `curl` command

```shell
# assuming your app is listening on 8080
ORDER_UUID=$(curl --silent -H 'Content-Type: application/json' -d "{\"itemName\":\"book\"}" http://localhost:8080/order | jq -r '.orderUuid') && for i in `seq 1 15`; do echo $(curl --silent "http://localhost:8080/order/status/"$ORDER_UUID); sleep 1; done;
```
Note: make sure you have already installed the `jq` 