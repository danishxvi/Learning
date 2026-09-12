# 26 · Bean Validation

> **Run the code for this lesson**
> ```bash
> mvn -f Spring/04-building-rest-apis-with-spring-mvc/26-bean-validation spring-boot:run
> ```

Lesson 16 proved `spring-boot-starter-validation` works, with a plain `Validator` bean
called by hand. This lesson wires the exact same library into a real REST endpoint, adds
nested validation, and writes a custom constraint from scratch.

---

## 1. `@Valid` is the switch — without it, constraints are decoration

```java
@PostMapping("/api/books")
public ResponseEntity<String> create(@Valid @RequestBody CreateBookRequest request) { ... }
```

Every `@NotBlank`, `@Min`, and custom constraint on `CreateBookRequest` does **nothing**
without `@Valid` on the parameter that receives it — Spring MVC only runs Bean Validation
when explicitly told to, right at the binding point. Forgetting `@Valid` is a real,
easy-to-make mistake: the annotations still compile, the IDE shows no warning, and every
constraint is silently skipped.

---

## 2. A structured, field-level error response

```bash
curl -X POST http://localhost:8080/api/books -H "Content-Type: application/json" -d '{
  "title":"","author":"","priceCents":-500,"isbn":"123",
  "publisher":{"name":"","contactEmail":"not-an-email"}
}'
```
```json
{
  "status":400,"error":"Validation Failed",
  "fieldErrors":[
    {"field":"author","rejectedValue":"","message":"author must not be blank"},
    {"field":"isbn","rejectedValue":"123","message":"must be a valid 13-digit ISBN"},
    {"field":"title","rejectedValue":"","message":"title must not be blank"},
    {"field":"priceCents","rejectedValue":"-500","message":"priceCents must not be negative"},
    {"field":"publisher.name","rejectedValue":"","message":"publisher name must not be blank"},
    {"field":"publisher.contactEmail","rejectedValue":"not-an-email","message":"publisher contact must be a well-formed email address"}
  ]
}
```

**Every violation in the request comes back at once** — not just the first one found.
This is `MethodArgumentNotValidException`, the specific exception Spring MVC throws when
`@Valid` finds any constraint violation, caught by a `@RestControllerAdvice`
(lesson 25's exact pattern):

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    List<...> fieldErrors = ex.getBindingResult().getFieldErrors().stream().map(this::toDetail).toList();
    ...
}
```

`ex.getBindingResult().getFieldErrors()` is where every individual violation lives —
field name, the value that was rejected, and the message, one entry per failed
constraint, across the whole request body.

---

## 3. Nested objects need their own `@Valid`

```java
public class CreateBookRequest {
    @Valid
    private PublisherInfo publisher;
}
```

`publisher.name` and `publisher.contactEmail` appear in the field-error list above only
because `@Valid` sits on the `publisher` field itself. **Validation does not cascade into
nested objects automatically** — without this `@Valid`, `PublisherInfo`'s own
`@NotBlank`/`@Email` constraints would never run at all, and an entirely invalid nested
publisher would pass silently. This is the same trap as forgetting `@Valid` on the
top-level parameter, one level deeper.

---

## 4. Writing a custom constraint

```java
@Constraint(validatedBy = IsbnValidator.class)
public @interface ValidIsbn {
    String message() default "must be a valid 13-digit ISBN";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

```java
public class IsbnValidator implements ConstraintValidator<ValidIsbn, String> {
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;   // let @NotNull/@NotBlank handle absence
        return value.matches("\\d{13}");
    }
}
```

This is **exactly how `@NotBlank` and `@Email` themselves are built** — an annotation
naming a `ConstraintValidator`, which implements one method deciding pass or fail. Once
declared, `@ValidIsbn` is used identically to any built-in constraint:

```java
@ValidIsbn
private String isbn;
```

`isValid` returning `true` for `null` is a real, standard convention — it lets each
constraint judge exactly one thing (`@ValidIsbn` judges *format*; `@NotBlank` judges
*presence*), so combining `@NotBlank @ValidIsbn` on the same field works correctly
instead of the two constraints fighting over what "invalid" means.

---

## 5. Summary

- **`@Valid`** on a `@RequestBody` parameter is what actually runs Bean Validation —
  every constraint on the target type does nothing without it, with no warning.
- A failed `@Valid` check throws **`MethodArgumentNotValidException`**, carrying every
  violation found, not just the first — caught with the same `@RestControllerAdvice`
  pattern lesson 25 introduced.
- **`ex.getBindingResult().getFieldErrors()`** is the source of a field-level, structured
  error response — field name, rejected value, and message, one entry per violation.
- **Nested objects need their own `@Valid`** on the field referencing them — validation
  does not cascade into a nested object automatically.
- **A custom constraint** is an annotation (`@Constraint(validatedBy = ...)`) naming a
  `ConstraintValidator` — the exact mechanism `@NotBlank` and `@Email` are built from,
  usable identically once declared.

---

**Previous:** [25 — Exception handling with `@ControllerAdvice`](../25-exception-handling-with-controlleradvice/25-exception-handling-with-controlleradvice.md) ·
**Next:** [27 — Pagination, sorting and filtering](../27-pagination-sorting-and-filtering/27-pagination-sorting-and-filtering.md)
