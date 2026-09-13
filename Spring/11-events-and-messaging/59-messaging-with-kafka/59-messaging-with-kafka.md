# 59 - Messaging with Kafka

RabbitMQ (lesson 58) routes each message to whichever queues match its
routing key, and once a queue delivers a message, that message is gone.
Kafka is different in a way that matters a lot in practice: every message
published to a topic is retained and delivered independently to **every
consumer group** that reads it, while consumers **within** the same group
share the work of a single, shared read. This lesson demonstrates both real
properties against a real Kafka broker - partitioning-by-key and
group-based fan-out/sharing - along with a genuine, common Spring Kafka
deserialization gotcha.

## Prerequisite: a real Kafka broker

```bash
docker run -d --name learning-kafka -p 9094:9094 \
  -e KAFKA_NODE_ID=1 -e KAFKA_PROCESS_ROLES=broker,controller \
  -e KAFKA_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093,EXTERNAL://:9094 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092,EXTERNAL://localhost:9094 \
  -e KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER \
  -e KAFKA_CONTROLLER_QUORUM_VOTERS=1@localhost:9093 \
  -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,EXTERNAL:PLAINTEXT \
  -e KAFKA_INTER_BROKER_LISTENER_NAME=PLAINTEXT \
  -e CLUSTER_ID=MkU3OEVBNTcwNTJENDM2Qk -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  apache/kafka:3.7.0
```

This is the official Apache Kafka image running in **KRaft mode** - modern
Kafka needs no separate Zookeeper process; a single container is a complete,
real broker. Started directly with `docker run`, same reasoning as lessons
56 and 58: the Docker CLI itself works fine on this machine, only
Testcontainers' Java client has the documented incompatibility from lesson
50.

```properties
spring.kafka.bootstrap-servers=localhost:9094
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.consumer.auto-offset-reset=earliest
```

```bash
mvn -f Spring/11-events-and-messaging/59-messaging-with-kafka spring-boot:run
```

## A real gotcha: `JsonDeserializer` rejects your own class by default

The first real run of this lesson - broker up, topology declared, JSON
(de)serializers configured - failed anyway, with every consumer logging the
same real exception:

```
Caused by: org.apache.kafka.common.errors.RecordDeserializationException: Error deserializing key/value for partition orders.topic-1 at offset 0.
Caused by: java.lang.IllegalArgumentException: The class 'com.danish.spring.kafka.OrderEvent' is not in the trusted packages: [java.util, java.lang]. If you believe this class is safe to deserialize, please provide its name. If the serialization is only done by a trusted source, you can also enable trust all (*).
```

**Why**: `JsonDeserializer` refuses to deserialize into arbitrary classes by
default, as a security measure (deserializing attacker-controlled JSON into
an attacker-chosen class is a known vector for exploits) - it only trusts
`java.util` and `java.lang` out of the box. `OrderEvent`, living in this
project's own package, isn't trusted until explicitly told to be. The fix,
in [`application.properties`](src/main/resources/application.properties):

```properties
spring.kafka.consumer.properties.spring.json.trusted.packages=com.danish.spring.kafka
```

With that one line, the exact same topic, the exact same producer, and the
exact same `@KafkaListener` methods worked without any other change.

**A second, real, and structurally interesting consequence** of hitting this
error during development: the poisoned messages (the ones that failed to
deserialize) were already sitting at offset 0 of their partitions. Simply
fixing the trusted-packages config and re-running was not enough - every
consumer immediately hit the same `RecordDeserializationException` again on
the very same still-unconsumable messages, because Kafka does not skip a
message a consumer failed to process; it retries the same offset
indefinitely unless something explicitly seeks past it. The topic had to be
deleted and recreated (`kafka-topics.sh --delete --topic orders.topic`,
recreated automatically by the app's `NewTopic` bean on the next startup) to
get a clean run. This is a genuine operational lesson in itself: unlike
RabbitMQ's per-message acknowledgment (lesson 58), a Kafka consumer that
can't process a message doesn't just drop it and move on - it's stuck at
that offset until the message is fixed, skipped, or the topic reset.

## Partition affinity: the same key always lands on the same partition

[`OrderEventProducer.java`](src/main/java/com/danish/spring/kafka/OrderEventProducer.java)
sends with an explicit key:

```java
public void send(String key, OrderEvent event) {
    kafkaTemplate.send(KafkaConfig.TOPIC, key, event);
}
```

Six events were sent across three keys to a topic declared with 3 partitions
([`KafkaConfig.java`](src/main/java/com/danish/spring/kafka/KafkaConfig.java) -
`TopicBuilder.name(TOPIC).partitions(3).replicas(1).build()`):

```java
String[] keys = {"order-1", "order-2", "order-1", "order-3", "order-2", "order-1"};
```

Real output:

```
partition affinity: does every message for the SAME key land on the SAME partition?
==========================================================================
  order-3 -> partitions [0]  (consistent - always the same partition)
  order-1 -> partitions [1]  (consistent - always the same partition)
  order-2 -> partitions [0]  (consistent - always the same partition)
```

Every one of `order-1`'s three messages landed on partition 1, every time;
`order-2`'s two messages and `order-3`'s one message both landed on
partition 0. Kafka hashes the key to deterministically pick a partition -
the same key always maps to the same partition on a given topic. This is
what gives Kafka **per-key ordering**: since a partition is read in strict
order by any one consumer, and a key's messages always land on the same
partition, all events for `order-1` are guaranteed to be processed in the
order they were sent - something the exchange/queue model in lesson 58
doesn't provide in the same way.

## Consumer groups: shared work within a group, independent copies across groups

Three listener beans consume the same topic -
[`GroupAConsumer1.java`](src/main/java/com/danish/spring/kafka/GroupAConsumer1.java)
and [`GroupAConsumer2.java`](src/main/java/com/danish/spring/kafka/GroupAConsumer2.java)
both in `groupId = "group-a"`, and
[`GroupBConsumer.java`](src/main/java/com/danish/spring/kafka/GroupBConsumer.java)
alone in `groupId = "group-b"`. Real output:

```
consumer groups: group-a SHARES messages, group-b gets its OWN full copy
==========================================================================
  messages sent: 6
  group-a total received (consumer-1=6 + consumer-2=0): 6  (matches - shared across the group, no duplicates)
  group-b total received: 6  (matches - its OWN full copy, independent of group-a)
```

`group-b`, the only consumer in its group, received all 6 messages - a
complete, independent copy of the topic's traffic. `group-a`'s two consumer
instances together also received exactly 6, with **no duplicates** - every
message was delivered to exactly one of them, never both, because Kafka
assigns each partition to only one consumer within a group. In this run, all
6 messages happened to land on partitions 0 and 1 (per the partition
affinity above), and Kafka's rebalance assigned both of those partitions to
`consumer-1`, leaving `consumer-2` with only the empty partition 2 - a real,
slightly uneven but entirely correct outcome, not a bug: partition
assignment balances *partitions* across consumers, not *messages*, so an
uneven message distribution across partitions can still produce an uneven
split between consumers even though the sharing itself worked exactly as
designed.

This is the core distinction from RabbitMQ's model: in RabbitMQ, one queue
means one logical consumer of that traffic (fan the same message to multiple
consumers only via multiple bound queues, as lesson 58 did with its topic
exchange). In Kafka, **the same topic** naturally supports both patterns at
once - `group-a` and `group-b` each got the full 6-message stream
independently, while consumers *inside* `group-a` split that stream between
themselves rather than duplicating it.

## Key takeaways

- A message's key deterministically picks its partition - the same key
  always lands on the same partition, which is what gives Kafka ordering
  guarantees *per key*, not across the whole topic.
- Consumers in the **same** consumer group share a topic's partitions -
  each message goes to exactly one consumer in the group, splitting the
  work. Consumers in **different** groups each get their own complete,
  independent copy of every message - the same topic serves both a
  load-balanced-queue pattern and a fan-out/pub-sub pattern simultaneously,
  depending only on `groupId`.
- `JsonDeserializer` only trusts `java.util`/`java.lang` by default and
  rejects application classes with a real `IllegalArgumentException` unless
  `spring.json.trusted.packages` explicitly allows them - a deliberate
  security default, not a bug.
- Kafka does not skip a message a consumer failed to deserialize or process
  - it retries the same offset indefinitely. A poisoned message blocks that
  consumer group's progress on that partition until it's fixed, explicitly
  skipped, or the topic is reset - unlike RabbitMQ, where a failed message
  can simply be rejected or dead-lettered per message.
