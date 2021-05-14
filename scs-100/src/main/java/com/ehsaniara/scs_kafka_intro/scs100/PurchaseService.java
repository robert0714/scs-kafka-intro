package com.ehsaniara.scs_kafka_intro.scs100;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseBinder purchaseBinder;

    /**
     * this is a simulating your order dataBase (Single App Instance), We can replaced it with KStream in feature tutorials.
     */
    Map<UUID, Order> orderDataBase = new HashMap<>();

    public OrderStatus statusCheck(UUID orderUuid) {
        return Optional.ofNullable(orderDataBase)
                .map(c -> c.get(orderUuid))
                .orElseThrow(() -> new OrderNotFoundException("Order not found")).getOrderStatus();
    }

    /**
     * here we placing the order into the topic and responding to the REST call immediately and not keep the thread busy much
     */
    public Order placeOrder(Order orderIn) {
        log.debug("placeOrder orderIn: {}", orderIn);

        var order = Order.builder()//
                .itemName(orderIn.getItemName())//
                .orderUuid(UUID.randomUUID())//
                .orderStatus(OrderStatus.PENDING)//
                .build();

        //update the status
        orderDataBase.put(order.getOrderUuid(), order);

        //send it for inventory check
        purchaseBinder.inventoryCheckingOut()//
                .send(MessageBuilder.withPayload(order)//
                        .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)//
                        .build());
        return order;
    }

    /**
     * check inventory System for Item availability.
     * this is a third party service simulation and
     * let say it tacks around 5 seconds to check your inventory
     */
    @StreamListener(PurchaseBinder.INVENTORY_CHECKING_IN)
    @SneakyThrows
    public void checkInventory(@Payload Order orderIn) {
        log.debug("checkInventory orderIn: {}", orderIn);
        orderIn.setOrderStatus(OrderStatus.INVENTORY_CHECKING);
        orderDataBase.put(orderIn.getOrderUuid(), orderIn);

        Thread.sleep(5_000);//5 sec delay

        // just a simulation of create exception for random orders (1 in 2) in case of inventory insufficiency
        if (System.currentTimeMillis() % 2 == 0) {
            orderIn.setOrderStatus(OrderStatus.INSUFFICIENT_INVENTORY);
            orderDataBase.put(orderIn.getOrderUuid(), orderIn);
            throw new OrderFailedException(String.format("insufficient inventory for order: %s", orderIn.getOrderUuid()));
        }

        //Order is good to go for shipping
        purchaseBinder.shippingOut()//
                .send(MessageBuilder.withPayload(orderIn)//
                        .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)//
                        .build());
    }

    /**
     * Order is shipped
     */
    @StreamListener(PurchaseBinder.SHIPPING_IN)
    public void shipIt(@Payload Order orderIn) {
        log.debug("shipIt orderIn: {}", orderIn);
        orderIn.setOrderStatus(OrderStatus.SHIPPED);
        orderDataBase.put(orderIn.getOrderUuid(), orderIn);

        log.info("ItemID: {} has been Shipped", orderIn.getOrderUuid());
    }

    /**
     * this is eventually a DLQ,
     * for a general purpose
     */
    @StreamListener(PurchaseBinder.ORDER_DLQ)
    public void cancelOrder(@Payload Order orderIn) {
        log.warn("cancelOrder orderIn: {}", orderIn);
        orderIn.setOrderStatus(OrderStatus.CANCELED);
        orderDataBase.put(orderIn.getOrderUuid(), orderIn);
    }
}