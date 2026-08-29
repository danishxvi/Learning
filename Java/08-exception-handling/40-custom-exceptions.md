# 40 · Custom Exceptions and Exception Design

> **Run the code for this lesson**
> ```bash
> java Java/08-exception-handling/40-custom-exceptions.java
> ```

Writing an exception class is trivial. Designing an exception *hierarchy* — one that helps
callers rather than burdening them — is the real skill.

---

## 1. Writing one

```java
public class InsufficientFundsException extends RuntimeException {

    private final BigDecimal shortfall;

    public InsufficientFundsException(String message, BigDecimal shortfall) {
        super(message);
        this.shortfall = shortfall;
    }

    public BigDecimal getShortfall() {
        return shortfall;
    }
}
```

Extend `RuntimeException` for unchecked, `Exception` for checked. That single choice is the
most consequential decision in the class.

### Provide the four standard constructors

`Throwable` has four, and a well-behaved exception mirrors them:

```java
public MyException() { super(); }
public MyException(String message) { super(message); }
public MyException(String message, Throwable cause) { super(message, cause); }
public MyException(Throwable cause) { super(cause); }
```

The `(String, Throwable)` one is the important one — without it, callers **cannot preserve
the cause** when wrapping (lesson 39), and your exception silently destroys stack traces.

---

## 2. Checked or unchecked?

The question to ask is:

> **Can a caller realistically do something different because of this?**

| Situation | Choose |
| --- | --- |
| The caller can retry, fall back, or prompt the user | **Checked** |
| It is a programming bug | **Unchecked** |
| The caller cannot meaningfully recover | **Unchecked** |
| It crosses a layer boundary and would leak an implementation detail | **Unchecked**, wrapping the cause |

**Modern Java leans heavily unchecked.** Spring, Hibernate and most frameworks converted
their checked exceptions to unchecked, because checked exceptions leak through
abstractions, force `throws` clauses up entire call stacks, and do not work with lambdas
(lesson 39 §8).

That said, checked exceptions are not a mistake — they are a tool with a narrow, real use:
**a failure the caller is expected to handle right there.** `IOException` on a file read is
a good example; the caller genuinely might retry or use a default.

---

## 3. Add data, not just a message

This is what separates a useful custom exception from a pointless one:

```java
// Pointless — a String would have done
throw new OrderException("order failed");

// Useful — the caller can act on it programmatically
throw new InsufficientFundsException(accountId, required, available);
```

With structured fields, a caller can:

```java
catch (InsufficientFundsException e) {
    if (e.getShortfall().compareTo(OVERDRAFT_LIMIT) < 0) {
        applyOverdraft(e.getShortfall());       // recover, with real data
    }
}
```

Parsing the message string to extract a number is a sign the exception was designed badly.

**Build the message from the fields** so it stays consistent:

```java
super("Account %s needs %s more".formatted(accountId, required.subtract(available)));
```

---

## 4. Hierarchy design

Give your module **one base exception**, then specialise:

```java
public class OrderException extends RuntimeException { }          // the base
public class OrderNotFoundException extends OrderException { }
public class InsufficientFundsException extends OrderException { }
public class PaymentDeclinedException extends OrderException { }
```

This gives callers a genuine choice of granularity:

```java
catch (InsufficientFundsException e) { ... }   // handle one case precisely
catch (OrderException e) { ... }               // or everything from this module
```

**Do not create an exception per error message.** Twenty exception classes that differ only
in their text is a code smell — use one class with a field, or an enum error code.

### Naming

End with `Exception`. Name the **problem**, not the code that failed:

- `InsufficientFundsException`, not `WithdrawFailedException`
- `OrderNotFoundException`, not `RepositoryException3`

---

## 5. Exceptions across layers

A repository throwing `SQLException` forces every caller to know you use SQL. Change to
MongoDB and the whole application breaks.

**Translate at the boundary, preserving the cause:**

```java
class UserRepository {
    User findById(long id) {
        try {
            return jdbc.query(...);
        } catch (SQLException e) {
            throw new DataAccessException("could not load user " + id, e);   // ← cause
        }
    }
}
```

Now the service layer depends on `DataAccessException`, not on JDBC. Spring's
`DataAccessException` hierarchy exists for exactly this reason.

**Never let a low-level exception escape a layer it does not belong to** — and never
translate without passing the cause.

---

## 6. Exceptions in constructors and validation

```java
Account(String id, BigDecimal balance) {
    this.id = Objects.requireNonNull(id, "id");
    if (balance.signum() < 0) {
        throw new IllegalArgumentException("balance cannot be negative: " + balance);
    }
    this.balance = balance;
}
```

`IllegalArgumentException` for a bad **argument**; `IllegalStateException` for a bad
**object state**; `NullPointerException` for an unexpected `null` — and
`Objects.requireNonNull` throws exactly that, with a message.

Do not invent `InvalidArgumentException`. The JDK's standard exceptions are well known and
callers already understand them.

---

## 7. Messages that help

A good message answers: **what failed, with what values, and what the caller might do.**

```java
// Useless
throw new IllegalArgumentException("invalid input");

// Useful
throw new IllegalArgumentException(
    "age must be between 0 and 150, got " + age);
```

Include the offending value. Do **not** include passwords, tokens, full card numbers or
personal data — exception messages end up in logs, and logs travel (lesson 32).

---

## 8. Documenting them

```java
/**
 * Withdraws money from this account.
 *
 * @param amount how much to withdraw; must be positive
 * @throws IllegalArgumentException  if the amount is not positive
 * @throws InsufficientFundsException if the balance is too low
 */
void withdraw(BigDecimal amount) { ... }
```

Document **unchecked** exceptions with `@throws` too. The compiler does not require it, but
it is the only way a caller learns what can happen — and it is the difference between an
API that can be used correctly and one that cannot.

---

## 9. Summary

- Extend `RuntimeException` for unchecked, `Exception` for checked. Provide the **four
  standard constructors** — especially `(String, Throwable)`, or callers cannot preserve
  the cause.
- Choose checked only when **the caller can realistically act on it**. Modern Java leans
  unchecked, and the major frameworks converted.
- **Carry structured data**, not just a message. If callers parse your message text, the
  design is wrong.
- Give a module **one base exception** and specialise beneath it — but do not create a
  class per error message.
- Name the **problem**, and end with `Exception`.
- **Translate at layer boundaries**, always passing the cause, so implementation details do
  not leak.
- Use the JDK's standard exceptions for argument, state and null problems rather than
  inventing equivalents.
- Messages should name the value that failed — and never contain secrets.
- Document unchecked exceptions with `@throws` anyway.

---

**Previous:** [39 — Exception handling basics](39-exception-handling-basics.md) ·
**Next:** [41 — try-with-resources](41-try-with-resources-and-best-practices.md)
