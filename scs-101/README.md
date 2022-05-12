
> This Project has 5 subprojects (stateless):

### scs-101-commons
[![scs-101 commons ci](https://github.com/robert0714/scs-kafka-intro/actions/workflows/scs101commons.yml/badge.svg?branch=main)](https://github.com/robert0714/scs-kafka-intro/actions/workflows/scs101commons.yml)

lib project for core projects

### 1- scs-101-inventory-check

[![scs-101 inventory-check ci](https://github.com/robert0714/scs-kafka-intro/actions/workflows/scs101inventorycheck.yml/badge.svg?branch=main)](https://github.com/robert0714/scs-kafka-intro/actions/workflows/scs101inventorycheck.yml)


### 2- scs-101-order

[![scs-101 order ci](https://github.com/robert0714/scs-kafka-intro/actions/workflows/scs101order.yml/badge.svg?branch=main)](https://github.com/robert0714/scs-kafka-intro/actions/workflows/scs101order.yml)

### 3- scs-101-order-branch

[![scs-101 order-branch ci](https://github.com/robert0714/scs-kafka-intro/actions/workflows/scs101orderbranch.yml/badge.svg?branch=main)](https://github.com/robert0714/scs-kafka-intro/actions/workflows/scs101orderbranch.yml)

### 4- scs-101-shipped

[![scs-101 shipped ci](https://github.com/robert0714/scs-kafka-intro/actions/workflows/scs101shipped.yml/badge.svg?branch=main)](https://github.com/robert0714/scs-kafka-intro/actions/workflows/scs101shipped.yml)

### 5- scs-101-shipping

[![scs-101 shipping ci](https://github.com/robert0714/scs-kafka-intro/actions/workflows/scs101shipping.yml/badge.svg?branch=main)](https://github.com/robert0714/scs-kafka-intro/actions/workflows/scs101shipping.yml)

# Compile
```shell
gradle  build --info
```
# Quick Start

```shell
ORDER_UUID=$(curl --silent -H 'Content-Type: application/json' -d "{\"itemName\":\"book\"}" http://localhost:8080/order | jq -r '.orderUuid') && for i in `seq 1 15`; do sleep 1; echo $(curl --silent "http://localhost:8080/order/status/"$ORDER_UUID); done;
```
# Build in local
1. First, we build them.
```shell
gradle build --stacktrac
docker-compose build
```
2. The Docker-compose file contains: single kafka and zookeeper. just simply run the following command

 
- Control Center UI
Runs a [Confluent Control Center](https://docs.confluent.io/platform/current/control-center/index.html) that exposes a UI at `http://localhost:9021/` .

```shell
docker-compose -f ./kafka-cluster.yml -f ./control-center-ui.yml up
```
To stop the brokers and the Control Center UI run the following command:
```shell
docker-compose -f ./kafka-cluster.yml -f ./control-center-ui.yml down
```

3. Start up the microservices landscape:

```shell
docker-compose up -d
```
We can follow the startup by monitoring the output :
```shell
docker-compose logs -f
```
> I assume you already have docker setup in your machine.


## Docker Images

same images for all projects tagged after the project name, since they are all in the same GitHub project, (but in the real scenario, they should be in a separate project and tagged by application version)
### scs-101-inventory-check
```shell
docker pull ghcr.io/robert0714/scs-kafka-intro:scs-101-inventory-check
```
### scs-101-order
```shell
docker pull ghcr.io/robert0714/scs-kafka-intro:scs-101-order
```
### scs-101-order-branch
```shell
docker pull ghcr.io/robert0714/scs-kafka-intro:scs-101-order-branch
```
### scs-101-shipped
```shell
docker pull ghcr.io/robert0714/scs-kafka-intro:scs-101-shipped
```
### scs-101-shipping
```shell
docker pull ghcr.io/robert0714/scs-kafka-intro:scs-101-shipping
```

# In Kubernetes
Create Order or Place your Order
you should now be able to place your order by calling the following curl command

```shll
# assuming your app order endpoint is burr.apps.ocp.iisi.test

SITE=burr.apps.ocp.iisi.test

ORDER_UUID=$(curl --silent -H 'Content-Type: application/json' -d "{\"itemName\":\"book\"}" http://$SITE/order | jq -r '.orderUuid') && for i in `seq 1 15`; do sleep 1; echo $(curl --silent "http://$SITE/order/status//"$ORDER_UUID); done;
```
Note: make sure you have already installed the jq