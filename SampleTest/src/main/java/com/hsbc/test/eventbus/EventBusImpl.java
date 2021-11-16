package com.hsbc.test.eventbus;


import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * EventBus which supports multiple producer/multiple consumer, handles all data types, subscribers with filters amd collated events
 */
public class EventBusImpl implements EventBus {

    private static Logger LOG = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    private final Map<Class<?>, Map<Subscriber, CopyOnWriteArraySet<Predicate<Object>>>> subscribers;
    private final LinkedBlockingQueue<Object> queue;
    private final Map<Class<? extends ConflatedEvent>, AtomicReference<ConflatedEvent>> conflatedEventsMap;
    private final ExecutorService executor;
    private volatile boolean shutdown = false;

    public EventBusImpl(int maxCapacity){
        subscribers = new ConcurrentHashMap<>();
        queue = new LinkedBlockingQueue<>(maxCapacity);
        conflatedEventsMap = new ConcurrentHashMap<>();
        executor = Executors.newSingleThreadExecutor();

        startSubscriptionExecutor();
    }

    public static void main(String[] args) throws InterruptedException {
        // orders are never conflated since they are not a conflated event
        //price ticks are conflated, if consumer is slow.
        testSlowConsumer();

    }

    private static void testSlowConsumer() throws InterruptedException {
        EventBusImpl eventBus = new EventBusImpl(100);

        eventBus.addSubscriber(Order.class, event -> LOG.info("Received order " + event));

        //price consumer is slow. so price ticks will be collated.
        eventBus.addSubscriber(Price.class, event -> {
            LOG.info("Received price " + event);
            try {
                Thread.sleep(100);// simulated slow consumer
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        eventBus.publishEvent(new Order(1L));
        eventBus.publishEvent(new Order(2L));

        //fast publisher
        eventBus.publishEvent(new Price(30L));
        eventBus.publishEvent(new Price(31L));
        eventBus.publishEvent(new Price(32L));
        eventBus.publishEvent(new Price(33L));
        eventBus.publishEvent(new Price(34L));
        eventBus.publishEvent(new Price(35L));

        Thread.sleep(1000);
        eventBus.shutDown();
    }

    public void shutDown() {
        shutdown = true;
        executor.shutdown();
        try{
            executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
        }catch(InterruptedException e){

        }finally{
            executor.shutdownNow();
        }

    }

    private void startSubscriptionExecutor() {
        executor.execute(() ->{
            while(!shutdown) {
                try {
                    final Object event = queue.poll(10, TimeUnit.MILLISECONDS);
                    if(event != null) {
                        if(event instanceof Event) {
                            sendToSubscribers((Event)event);
                        }else if(event instanceof AtomicReference) {
                            ConflatedEvent conflatedEvent = (ConflatedEvent)(((AtomicReference) event).get());
                            conflatedEventsMap.remove(conflatedEvent.getClass());
                            sendToSubscribers(conflatedEvent);
                        }
                    }
                }catch(InterruptedException e){
                    LOG.log(Level.SEVERE, e.toString(), e);
                }
            }
        });
    }

    private void sendToSubscribers(Event event) {
        Map<Subscriber, CopyOnWriteArraySet<Predicate<Object>>> subscribersWithFilters = subscribers.get(event.getClass());
        subscribersWithFilters.entrySet().stream().forEach(e -> {
            Subscriber subscriber = e.getKey();
            for (Predicate<Object> filter : e.getValue()) {
                if (filter.test(event)) {
                    subscriber.onMessage(event);
                }
            }
        });
    }

    @Override
    public void publishEvent(Object event) {
        if(event instanceof ConflatedEvent) {
            ConflatedEvent conflatedEvent = (ConflatedEvent) event;

            AtomicReference<ConflatedEvent> reference = conflatedEventsMap.putIfAbsent(conflatedEvent.getClass(), new AtomicReference<>(conflatedEvent));
            if(reference == null) {
                queue.offer(conflatedEventsMap.get(conflatedEvent.getClass()));
            } else {
                conflatedEventsMap.get(conflatedEvent.getClass()).getAndUpdate(e -> conflatedEvent);
            }
        }else {
            queue.offer(event);
        }
    }

    @Override
    public void addSubscriber(Class<? extends Event> clazz, Subscriber subscriber) {
        getSubscribers(clazz, subscriber).add(p -> true);
    }


    @Override
    public void addSubscriberForFilteredEvents(Class<? extends Event> clazz, Subscriber subscriber, Predicate<Object> filter) {
        getSubscribers(clazz, subscriber).add(filter);
    }

    private CopyOnWriteArraySet<Predicate<Object>> getSubscribers(Class<?> clazz, Subscriber subscriber) {
        return subscribers.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>())
                            .computeIfAbsent(subscriber, k2 -> new CopyOnWriteArraySet<>());
    }
}
