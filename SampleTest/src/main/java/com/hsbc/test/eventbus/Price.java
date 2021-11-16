package com.hsbc.test.eventbus;

public class Price implements ConflatedEvent {
    private long tickId;

    public Price(long tickId) {
        this.tickId = tickId;
    }

    @Override
    public String toString() {
        return "Price{" +
                "tickId=" + tickId +
                '}';
    }
}
