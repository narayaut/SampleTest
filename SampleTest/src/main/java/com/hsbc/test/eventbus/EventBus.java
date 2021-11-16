package com.hsbc.test.eventbus;

import java.util.function.Predicate;

public interface EventBus {

    void publishEvent(Object event);

    void addSubscriber(Class<? extends Event> clazz, Subscriber subscriber);

    void addSubscriberForFilteredEvents(Class<? extends Event> clazz, Subscriber subscriber, Predicate<Object> filter);


    interface Subscriber {
        void onMessage(Object event);
    }
}
