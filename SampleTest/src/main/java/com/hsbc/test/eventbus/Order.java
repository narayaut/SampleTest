package com.hsbc.test.eventbus;

public class Order implements Event {
    private long orderId;

    public Order(long id) {
        this.orderId = id;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                '}';
    }
}
