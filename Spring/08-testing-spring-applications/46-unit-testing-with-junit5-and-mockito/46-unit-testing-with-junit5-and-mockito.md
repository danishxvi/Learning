# 46 · Unit testing with JUnit 5 and Mockito

> **Run the tests for this lesson**
> ```bash
> mvn -f Spring/08-testing-spring-applications/46-unit-testing-with-junit5-and-mockito test
> ```

Lesson 04's smoke test used `@SpringBootTest` to start the whole `ApplicationContext`
just to check nothing was broken. This lesson is the opposite kind of test entirely: no
Spring context at all, testing one class's logic in complete isolation, in
milliseconds.

---

## 1. `spring-boot-starter-test` — one dependency, an entire toolkit

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

This single starter is why lesson 04's smoke test needed nothing added — it bundles
JUnit 5, Mockito, AssertJ, and Spring's own test support together. Everything in this
lesson comes from it.

---

## 2. No `ApplicationContext` at all

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock private OrderRepository orderRepository;
    @Mock private PricingService pricingService;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, pricingService);
    }
}
```

**`@ExtendWith(MockitoExtension.class)`, not `@SpringBootTest`.** No bean scanning, no
auto-configuration, none of lesson 04's 52 mystery beans — `orderService` is built with
a plain `new`, exactly the way lesson 01 first demonstrated constructor injection
working with no framework at all. Running all six tests in this class took **1.5
seconds total** — most of that is JVM/JUnit startup, not application logic. This is
what makes a true unit test valuable as a fast feedback loop: no server, no database,
no context refresh, just the one class's behavior.

---

## 3. `@Mock` — a fake with no real behavior until told otherwise

```java
when(pricingService.unitPriceCents("desk-lamp")).thenReturn(2499);
when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
    Order order = invocation.getArgument(0);
    order.setId(42L);
    return order;
});
```

`@Mock` creates an object that implements the interface but does nothing on its own —
every method returns `null`/`0`/`false` until a `when(...).thenReturn(...)` (or, for
logic that depends on the argument, `.thenAnswer(...)`) stubs it. Neither a real
database nor real pricing logic exists anywhere in this test — `OrderService` cannot
tell the difference between this and its real dependencies, which is the entire point.

---

## 4. `verify()` — checking a mock was actually called

```java
verify(orderRepository, times(1)).save(any(Order.class));
...
verify(orderRepository, never()).save(any());
...
verifyNoInteractions(pricingService, orderRepository);
```

Stubbing (`when`) controls what a mock *returns*; `verify` checks what was actually
*called*, and how many times. `verifyNoInteractions` on both mocks together proves the
invalid-quantity check fails **fast** — neither dependency is ever touched, because the
guard clause runs before either of them in `OrderService.placeOrder`.

---

## 5. `ArgumentCaptor` — inspecting exactly what was passed

```java
ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
verify(orderRepository).save(captor.capture());
Order saved = captor.getValue();

assertThat(saved.getProduct()).isEqualTo("keyboard");
assertThat(saved.getTotalCents()).isEqualTo(9998);
```

`any(Order.class)` in a stub or `verify` only checks *that* some `Order` was passed —
`ArgumentCaptor` captures the actual object, so the test can assert on its real field
values. This is how a test confirms not just "`save` was called" but "`save` was called
with the *correct* data."

---

## 6. A real failure, then the fix

Deliberately changing this test's expected total from `9998` to `9999` and re-running
produces this, real and unedited:

```
org.opentest4j.AssertionFailedError:

expected: 9999
 but was: 9998
	at com.danish.spring.unittesting.OrderServiceTest.capturesTheSavedOrder(OrderServiceTest.java:78)
```

```
[ERROR] Tests run: 6, Failures: 1, Errors: 0, Skipped: 0
[INFO] BUILD FAILURE
```

AssertJ's `assertThat(...).isEqualTo(...)` produces exactly this shape of message —
expected vs. actual, with the failing line number — and Maven's Surefire plugin fails
the whole build the moment any test fails, not just this one method. Reverting the
value back to `9998` returns the suite to `Tests run: 6, Failures: 0` and
`BUILD SUCCESS`.

---

## 7. `@ParameterizedTest` — one test body, many inputs

```java
@ParameterizedTest(name = "quantity {0} is rejected before pricing or saving ever run")
@ValueSource(ints = {0, -1, -100})
void rejectsInvalidQuantities(int badQuantity) {
    assertThrows(IllegalArgumentException.class, () -> orderService.placeOrder("desk-lamp", badQuantity));
    verifyNoInteractions(pricingService, orderRepository);
}
```

Real output treats this as **three separate tests**, one per value in `@ValueSource`,
each with its own name and pass/fail result — not one test looping internally and
hiding which specific input failed if something breaks.

---

## 8. Summary

- **`spring-boot-starter-test`** bundles JUnit 5, Mockito, and AssertJ in one
  dependency — nothing else needs to be added for any of this.
- **A pure unit test needs no `ApplicationContext`** — `@ExtendWith(MockitoExtension.class)`
  and plain constructor injection are enough, and the whole suite ran in about 1.5
  seconds.
- **`@Mock` + `when(...).thenReturn(...)`** replaces a real dependency with a
  controllable fake; **`verify(...)`** checks it was actually called, with what
  arguments, how many times.
- **`ArgumentCaptor`** inspects the real object a mock received, for assertions
  `any(...)` alone can't express.
- **A failing assertion produces a real `AssertionFailedError`** with an expected/actual
  diff and exact line number — verified here by deliberately breaking, then fixing, one.
- **`@ParameterizedTest`** runs one test body against many inputs, reported as
  genuinely separate test results, not a single pass/fail for the whole loop.

---

**Previous:** [45 — CORS and CSRF](../../07-spring-security/45-cors-and-csrf/45-cors-and-csrf.md) ·
**Next:** [47 — Testing the web layer with `@WebMvcTest`](../47-testing-the-web-layer-with-webmvctest/47-testing-the-web-layer-with-webmvctest.md)
