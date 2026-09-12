# 01 · Why Spring — the problem dependency injection solves

> **Run the code for this lesson** (plain Java — no Maven yet)
> ```bash
> java Spring/01-introduction-and-setup/01-why-spring-the-problem-di-solves/01-why-spring-the-problem-di-solves.java
> ```

Before touching a single Spring annotation, you need to feel the problem Spring exists to
solve. Every Spring feature — `@Autowired`, `@Component`, the `ApplicationContext`,
auto-configuration — is automation built on top of one idea: **Dependency Injection
(DI)**. If you understand DI without a framework, Spring stops looking like magic and
starts looking like a very well-engineered box of tools for a problem you already
recognise.

---

## 1. The problem: tight coupling

A class is **tightly coupled** to another class when it creates that class itself, with
`new`, from the inside:

```java
class OrderService {
    private final SmtpEmailSender emailSender = new SmtpEmailSender(); // created HERE

    void placeOrder(String product) {
        emailSender.send("Your order for " + product + " has shipped.");
    }
}
```

This looks harmless. It causes three real problems:

1. **You cannot test it in isolation.** Testing `placeOrder` also tests `SmtpEmailSender`,
   which means a real (or mocked-at-the-network-layer) SMTP connection just to check that
   an order was placed.
2. **You cannot swap the implementation.** Want to send an SMS instead, or log to a file
   during development? You have to open `OrderService` and edit it.
3. **The `new` is repeated everywhere.** Every class that needs to send a notification
   writes its own `new SmtpEmailSender()`, so a change to that constructor ripples through
   the whole codebase.

---

## 2. Half a fix: programming to an interface

The obvious first move is to depend on an interface instead of a concrete class:

```java
interface Notifier {
    void send(String message);
}

class OrderService {
    private final Notifier notifier = new SmtpEmailNotifier(); // STILL created here
    ...
}
```

This helps *type* coupling — code that calls `notifier.send(...)` doesn't care which
class it's talking to — but it does not fix the actual problem. `OrderService` still
decides, at compile time, which `Notifier` it gets, because the `new` is still inside it.
Interfaces alone are not dependency injection.

---

## 3. The real fix: Dependency Injection

Move the `new` **outside** the class, and hand the dependency in through the constructor:

```java
class OrderService {
    private final Notifier notifier;

    OrderService(Notifier notifier) {   // INJECTED, not created
        this.notifier = notifier;
    }

    void placeOrder(String product) {
        notifier.send("Your order for " + product + " has shipped.");
    }
}
```

```java
OrderService service = new OrderService(new SmtpEmailNotifier());  // email in production
OrderService test    = new OrderService(msg -> capturedMessages.add(msg)); // fake in tests
```

`OrderService` now declares *what it needs* (something that implements `Notifier`) and
has no opinion about *which one it gets*. That single inversion — the class no longer
controls its own dependencies, the caller does — is called **Inversion of Control (IoC)**,
and handing the dependency in through the constructor is **constructor injection**. This
is the entire idea. There is no framework in this code at all yet.

---

## 4. The chore DI creates: wiring

Dependency Injection fixes coupling, but it moves the problem: now *something* has to
actually build the whole object graph, in the right order, and pass every piece to the
next constructor:

```java
Notifier notifier               = new SmtpEmailNotifier();
InventoryChecker inventory      = new InventoryChecker();
PaymentGateway gateway          = new StripePaymentGateway();
PaymentService paymentService   = new PaymentService(gateway, notifier);
Checkout checkout               = new Checkout(paymentService, inventory, notifier);
```

Five classes, six lines, and the lines are **order-dependent** — `PaymentService` must be
built before `Checkout`, because `Checkout` needs it. This hand-written assembly code is
called **the wiring**. In a real application with hundreds of classes, the wiring code
becomes its own maintenance burden: every time a constructor's parameter list changes,
every place that builds that class by hand has to change too.

---

## 5. What a container does about it

An **IoC container** (also called a DI container) is a piece of infrastructure whose only
job is wiring. You tell it, once, *how* to build each type; it figures out the order and
hands you finished objects on request. The code in this lesson builds a forty-line toy
version — `MiniContainer` — to make this concrete before Spring's real
`ApplicationContext` shows up in lesson 06:

```java
container.register(Notifier.class, c -> new SmtpEmailNotifier());
container.register(PaymentService.class,
        c -> new PaymentService(c.get(PaymentGateway.class), c.get(Notifier.class)));
container.register(Checkout.class,
        c -> new Checkout(c.get(PaymentService.class), c.get(InventoryChecker.class), c.get(Notifier.class)));

Checkout checkout = container.get(Checkout.class);  // fully wired, in one call
```

Registration order does not matter — `get` resolves dependencies **recursively**, and
caches the result so a second `get(Checkout.class)` returns the exact same instance
instead of rebuilding it.

This toy uses a `HashMap` of factory lambdas. Spring's real container uses **reflection**
to read a class's constructor parameters and **annotations** (`@Component`, `@Autowired`)
to know which classes to manage in the first place — but the shape of the idea, "given a
type, build or reuse an instance, resolving its dependencies recursively," is identical.
Nothing Spring does here is conceptually new; it is the same forty lines, generalised and
automated.

---

## 6. Why this matters before you write a line of Spring

Every confusing Spring error you will eventually hit —
`NoSuchBeanDefinitionException`, `NoUniqueBeanDefinitionException`,
`BeanCurrentlyInCreationException` (a circular dependency) — is the container failing at
exactly the job `MiniContainer.get` does above: finding, or building, or ordering the
construction of, a bean. Knowing what the container is *for* makes those errors legible
instead of mysterious.

---

## 7. Summary

- **Tight coupling**: a class creates its own dependency with `new`, so it cannot be
  tested or swapped without editing it.
- **Programming to an interface** fixes the *type* but not the *creation* — the `new` has
  to move somewhere else too.
- **Dependency Injection**: a class declares what it needs as a constructor parameter; the
  caller decides what to pass. The class no longer controls its own dependencies.
- **Inversion of Control**: the name for that shift in who's in charge of construction.
- DI does not eliminate wiring — it moves it out of the class and into whoever assembles
  the object graph, which becomes its own chore at scale.
- An **IoC container** automates that chore: register how to build each type once, then
  ask it for finished, fully-wired objects.
- **Spring's `ApplicationContext` is exactly this idea**, built out with reflection,
  annotations, scopes and lifecycle hooks — covered starting in section 02.

---

**Next:** [02 — Spring vs Spring Boot, and the ecosystem around them](../02-spring-vs-spring-boot-and-the-ecosystem/02-spring-vs-spring-boot-and-the-ecosystem.md)
