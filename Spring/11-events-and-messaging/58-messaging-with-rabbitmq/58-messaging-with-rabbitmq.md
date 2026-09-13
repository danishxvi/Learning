# 58 - Messaging with RabbitMQ

Application events (lesson 57) are in-process: a publisher and its listeners
live in the same JVM, and everything stops existing the moment that process
does. RabbitMQ is a real, separate broker process - messages are published to
an **exchange**, routed to **queues** based on a **routing key**, and
consumed by whatever's listening, entirely independent of whether the
publisher and consumer are even the same application. Every scenario below
ran against a real RabbitMQ broker, not a mock.

## Prerequisite: a real RabbitMQ broker

```bash
docker run -d --name learning-rabbitmq -p 5673:5672 -p 15673:15672 rabbitmq:3-management-alpine
```

(Started directly with `docker run`, not Testcontainers - see lesson 56 for
why: the Docker CLI itself works fine on this machine, only Testcontainers'
Java client has the documented incompatibility from lesson 50.)

`application.properties` points at it:

```properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5673
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

```bash
mvn -f Spring/11-events-and-messaging/58-messaging-with-rabbitmq spring-boot:run
```

## Declaring topology as beans

[`RabbitConfig.java`](src/main/java/com/danish/spring/rabbitmq/RabbitConfig.java)
declares one topic exchange and three queues, bound with different routing
patterns:

```java
@Bean
public TopicExchange ordersExchange() { return new TopicExchange(EXCHANGE); }

@Bean
public Binding allBinding() {
    return BindingBuilder.bind(allQueue()).to(ordersExchange()).with("order.#");
}

@Bean
public Binding createdBinding() {
    return BindingBuilder.bind(createdQueue()).to(ordersExchange()).with("order.created");
}

@Bean
public Binding shippedBinding() {
    return BindingBuilder.bind(shippedQueue()).to(ordersExchange()).with("order.shipped");
}
```

`order.#` (topic exchange wildcard: `#` matches any number of words) matches
every routing key starting with `order.` - `orders.all.queue` gets
everything. `order.created` and `order.shipped` are exact matches - their
queues only get messages published with that precise routing key. No manual
setup step is needed against the broker: Spring Boot's
`RabbitAutoConfiguration` wires in a `RabbitAdmin` that declares every
`Queue`/`Exchange`/`Binding` bean against the real broker on startup.
Verified directly against RabbitMQ after a run, independent of the
application:

```bash
docker exec learning-rabbitmq rabbitmqctl list_bindings source_name routing_key destination_name
```
```
source_name       routing_key      destination_name
                  orders.all.queue     orders.all.queue
                  orders.created.queue orders.created.queue
                  orders.shipped.queue orders.shipped.queue
orders.exchange   order.#              orders.all.queue
orders.exchange   order.created        orders.created.queue
orders.exchange   order.shipped        orders.shipped.queue
```

This is real broker state, inspected with a tool that has never heard of
Spring - proof the declarations in Java code actually created this topology,
not just an assumption about what `@Bean` methods "should" do.

## A real gotcha: the default converter rejects a plain record

[`OrderEvent.java`](src/main/java/com/danish/spring/rabbitmq/OrderEvent.java)
is a plain record, deliberately not `Serializable`:

```java
public record OrderEvent(String orderId, String status) {
}
```

[`OrderEventPublisher.java`](src/main/java/com/danish/spring/rabbitmq/OrderEventPublisher.java)
sends it with `rabbitTemplate.convertAndSend(EXCHANGE, routingKey, event)`.
The **first real run of this lesson, with no custom `MessageConverter`
bean**, failed immediately:

```
java.lang.IllegalArgumentException: SimpleMessageConverter only supports String, byte[] and Serializable payloads, received: com.danish.spring.rabbitmq.OrderEvent
	at org.springframework.amqp.support.converter.SimpleMessageConverter.createMessage(SimpleMessageConverter.java:143)
	at org.springframework.amqp.rabbit.core.RabbitTemplate.convertAndSend(RabbitTemplate.java:1188)
	at com.danish.spring.rabbitmq.OrderEventPublisher.publish(OrderEventPublisher.java:16)
```

**Why**: with no explicit `MessageConverter` bean, `RabbitTemplate` falls
back to `SimpleMessageConverter`, which serializes the message body using
plain Java serialization - it requires the payload to implement
`java.io.Serializable`. A Java record does **not** implement `Serializable`
automatically just by being a record; `OrderEvent` genuinely doesn't
implement it, and the converter correctly refused to send it.

**The fix** - [`RabbitMessageConverterConfig.java`](src/main/java/com/danish/spring/rabbitmq/RabbitMessageConverterConfig.java):

```java
@Bean
public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
}
```

Spring Boot's `RabbitAutoConfiguration` automatically wires **any**
`MessageConverter` bean into both the `RabbitTemplate` (for sending) and the
default `@RabbitListener` container factory (for receiving) - no extra
configuration needed on either side. With this one bean added, the exact
same `OrderEvent` record now serializes to JSON instead of requiring
`Serializable`, and the exact same publish call succeeds.

## Topic routing, verified by which listener actually received what

[`AllOrdersListener.java`](src/main/java/com/danish/spring/rabbitmq/AllOrdersListener.java) /
[`CreatedOrdersListener.java`](src/main/java/com/danish/spring/rabbitmq/CreatedOrdersListener.java) /
[`ShippedOrdersListener.java`](src/main/java/com/danish/spring/rabbitmq/ShippedOrdersListener.java)
each listen to one queue:

```java
@RabbitListener(queues = RabbitConfig.CREATED_QUEUE)
public void onMessage(OrderEvent event) {
    recorder.record("created-queue received " + event);
}
```

The demo publishes two events with different routing keys:

```java
publisher.publish("order.created", new OrderEvent("order-1", "CREATED"));
publisher.publish("order.shipped", new OrderEvent("order-1", "SHIPPED"));
```

Real output:

```
publishing order.created and order.shipped to a topic exchange
==========================================================================
  entries received by listeners:
    created-queue received OrderEvent[orderId=order-1, status=CREATED]
    all-queue received OrderEvent[orderId=order-1, status=CREATED]
    shipped-queue received OrderEvent[orderId=order-1, status=SHIPPED]
    all-queue received OrderEvent[orderId=order-1, status=SHIPPED]
```

Exactly four deliveries, exactly matching the bindings: the `CREATED` event
reached `created-queue` (exact match on `order.created`) and `all-queue`
(wildcard match on `order.#`), but **not** `shipped-queue`. The `SHIPPED`
event reached `shipped-queue` and `all-queue`, but **not** `created-queue`.
Routing key matching against the topic exchange genuinely filtered delivery
- nothing crossed over. Confirmed independently after the run: both queues
show `0` remaining messages (everything was consumed), proving the messages
really traveled through the broker rather than being short-circuited
in-process:

```bash
docker exec learning-rabbitmq rabbitmqctl list_queues name messages
```
```
name                  messages
orders.all.queue      0
orders.created.queue  0
orders.shipped.queue  0
```

## Key takeaways

- `Queue`, `TopicExchange`, and `Binding` beans are enough to declare
  messaging topology - Spring Boot's `RabbitAdmin` creates them against the
  real broker automatically on startup, verifiable independently with
  `rabbitmqctl`.
- A topic exchange's routing key matching (`#` for any number of words, `*`
  for exactly one) genuinely filters which queues receive a given message -
  demonstrated here by one event reaching two queues and being correctly
  excluded from a third.
- Spring AMQP's default `SimpleMessageConverter` requires
  `java.io.Serializable` payloads; a plain record fails against it with a
  real `IllegalArgumentException`. A `Jackson2JsonMessageConverter` bean
  fixes this for both sending and receiving, with Spring Boot wiring it into
  both automatically - the same class of gotcha as lesson 56's missing
  `jackson-databind` dependency, just surfacing at a different layer.
- RabbitMQ is a real, separate, inspectable process - `rabbitmqctl` can
  confirm queue depth and bindings independently of the application, the
  same way `redis-cli` did for the Redis cache in lesson 56.
