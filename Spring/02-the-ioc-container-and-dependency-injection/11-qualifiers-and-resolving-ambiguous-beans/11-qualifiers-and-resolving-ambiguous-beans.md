# 11 · Qualifiers and resolving ambiguous beans

> **Run the code for this lesson**
> ```bash
> mvn -f Spring/02-the-ioc-container-and-dependency-injection/11-qualifiers-and-resolving-ambiguous-beans spring-boot:run
> ```

This lesson's context has **three** `Notifier` implementations. Asking for "the"
`Notifier` is inherently ambiguous — this lesson reproduces the real failure, then shows
four different ways to resolve it, each suited to a different situation.

---

## 1. The ambiguity, unresolved

```java
public CheckoutNeedingUnqualifiedGateway(PaymentGateway gateway) { }
```

Registered alongside two plain `PaymentGateway` implementations with no `@Primary` and no
`@Qualifier` anywhere, startup fails with the exact, unedited exception:

```
NoUniqueBeanDefinitionException: No qualifying bean of type
'com.danish.spring.qualifiers.PaymentGateway' available:
expected single matching bean but found 2: stripeGateway, paypalGateway
```

This is Spring refusing to guess. Two beans satisfy the type; nothing in the code says
which one this constructor parameter wants, so the container fails loudly at startup
rather than picking one arbitrarily and hoping it's the right one.

---

## 2. `@Primary` — a default for unqualified injection

```java
@Primary
@Component
public class EmailNotifier implements Notifier { ... }
```

```java
public PrimaryConsumer(Notifier notifier) { ... }   // no @Qualifier at all
```

With three `Notifier` beans in the context, this constructor still resolves cleanly —
`resolvedType()` prints `EmailNotifier`. `@Primary` marks one candidate as "the default
when nothing more specific is asked for." **At most one bean per type can be `@Primary`**
— marking two would just move the ambiguity error from "no primary" to "two primaries,"
which Spring rejects at startup exactly like it rejected zero.

---

## 3. `@Qualifier` — naming a specific bean, overriding `@Primary`

```java
public QualifiedConsumer(@Qualifier("smsNotifier") Notifier notifier) { ... }
```

`resolvedType()` here prints `SmsNotifier` — **not** `EmailNotifier`, even though
`EmailNotifier` is still `@Primary`. `@Qualifier` names an exact bean and always wins over
`@Primary` at the injection point that uses it; `@Primary` only applies where nothing more
specific was requested. The name `"smsNotifier"` is the bean's default name — a
`@Component` class's simple name with a lowercase first letter — unless overridden with
`@Component("customName")`.

**Rule of thumb**: `@Primary` for "this is the sensible default everywhere"; `@Qualifier`
for "this one specific place needs a specific, non-default implementation."

---

## 4. Sidestepping ambiguity entirely: inject all of them

```java
public BroadcastNotifier(List<Notifier> allNotifiers, Map<String, Notifier> byBeanName) { ... }
```

Neither collection type is ambiguous — there's no "pick one" decision to make, because
both ask for **every** matching bean:

```
List<Notifier>            -> [PushNotifier, EmailNotifier, SmsNotifier]
Map<String, Notifier>     -> { emailNotifier: EmailNotifier, pushNotifier: PushNotifier, smsNotifier: SmsNotifier }
```

The `List` is **ordered**; the `Map` is keyed by bean name and carries no ordering
guarantee of its own. This is the natural fit for "run every registered handler," "try
each strategy in order," or "look one up dynamically by name" — the plugin-style patterns
where "ambiguous" was never actually the right framing to begin with.

---

## 5. Controlling list order with `@Order`

```java
@Order(1)  public class PushNotifier  implements Notifier { }
@Order(2)  public class EmailNotifier implements Notifier { }   // also @Primary - independent concerns
@Order(3)  public class SmsNotifier   implements Notifier { }
```

The `List<Notifier>` above comes out `[PushNotifier, EmailNotifier, SmsNotifier]` —
**lowest `@Order` value first**. `@Order` and `@Primary` solve two unrelated problems:
`@Primary` decides what a single, unqualified injection gets; `@Order` decides what
sequence a *collection* of them comes out in. A bean can carry both, as `EmailNotifier`
does here, with no interaction between them.

---

## 6. Summary

- Two or more beans implementing the same type, injected with no further hint, is a
  startup-time `NoUniqueBeanDefinitionException` — Spring never guesses.
- **`@Primary`** marks one bean as the default for unqualified injection points — at most
  one per type.
- **`@Qualifier("beanName")`** names a specific bean at a specific injection point, and
  overrides `@Primary` there.
- **`List<T>`** and **`Map<String, T>`** injection points ask for every matching bean at
  once — ambiguity never applies to them.
- **`@Order`** controls the sequence of a `List<T>` injection (lowest value first) and is
  unrelated to `@Primary`, which only affects single-bean resolution.

---

**Previous:** [10 — Bean scopes](../10-bean-scopes/10-bean-scopes.md) ·
**Next:** [12 — Conditional beans and profiles](../12-conditional-beans-and-profiles/12-conditional-beans-and-profiles.md)
