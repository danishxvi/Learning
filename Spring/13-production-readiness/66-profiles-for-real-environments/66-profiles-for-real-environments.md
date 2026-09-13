# 66 - Profiles for Real Environments

`@Value` and `application.properties` change what a *value* is per
environment. `@Profile` goes further: it changes which *beans exist in the
container at all*. This lesson demonstrates both - real config values
switching per environment, and a real, genuine `NoSuchBeanDefinitionException`
that happens when no profile matches anything.

```bash
mvn -f Spring/13-production-readiness/66-profiles-for-real-environments spring-boot:run -Dspring-boot.run.profiles=dev
```

## Profile-specific property files

Three files, loaded by Spring Boot's naming convention -
`application.properties` always loads;
`application-<profile>.properties` loads only when that profile is active
and its values override the base file's:

```properties
# application.properties (base - always loaded)
app.environment=no-profile-active
```
```properties
# application-dev.properties
app.environment=dev
notification.provider=console (dev)
```
```properties
# application-prod.properties
app.environment=prod
notification.provider=real paid SMS/email gateway (prod)
```

## `@Profile` controls bean EXISTENCE, not just values

[`DevNotificationSender.java`](src/main/java/com/danish/spring/profiles/DevNotificationSender.java)
and [`ProdNotificationSender.java`](src/main/java/com/danish/spring/profiles/ProdNotificationSender.java)
both implement the same `NotificationSender` interface, each gated by a
different profile - and **deliberately, there is no default, profile-less
implementation**:

```java
@Profile("dev")
@Component
public class DevNotificationSender implements NotificationSender {
    public String send(String message) {
        return "[DEV] printed to console instead of really sending: " + message;
    }
}
```
```java
@Profile("prod")
@Component
public class ProdNotificationSender implements NotificationSender {
    public String send(String message) {
        return "[PROD] actually dispatched via the real paid gateway: " + message;
    }
}
```

## The real gotcha: no active profile means the bean genuinely does not exist

Running with no profile specified at all - a perfectly easy mistake, and
exactly what happens if a deployment forgets to set
`SPRING_PROFILES_ACTIVE` - produced a real, immediate startup failure:

```
Description:

Parameter 1 of constructor in com.danish.spring.profiles.ProfilesApplication$Demo required a bean of type 'com.danish.spring.profiles.NotificationSender' that could not be found.

Action:

Consider defining a bean of type 'com.danish.spring.profiles.NotificationSender' in your configuration.
```

**This is not a configuration-value problem** - it's not that
`notification.provider` was blank or defaulted to something wrong. Neither
`DevNotificationSender` nor `ProdNotificationSender` was ever *instantiated*
at all, because Spring evaluates `@Profile` conditions before bean creation,
and with the "default" profile active (Spring Boot's fallback when nothing
is specified), neither `"dev"` nor `"prod"` matched. The
`NotificationSender` interface genuinely has zero implementations in the
container - not a null, not a default, a bean that doesn't exist, and
Spring's dependency injection correctly refuses to start rather than run
with a missing dependency.

## With a profile active: the right bean, and the right config, together

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
```
active profiles: [dev]
app.environment: dev
notificationSender bean: DevNotificationSender
[DEV] printed to console instead of really sending: order #123 shipped
```

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```
```
active profiles: [prod]
app.environment: prod
notificationSender bean: ProdNotificationSender
[PROD] actually dispatched via the real paid gateway: order #123 shipped
```

Same code, same interface, same call site
(`notificationSender.send("order #123 shipped")`) - genuinely different
concrete beans wired in, confirmed both by `getClass().getSimpleName()` and
by each implementation's own real output. This is the actual point of
profiles in a real deployment: a `dev` environment can wire up a fake,
free, side-effect-free stand-in for something a `prod` environment needs to
wire up for real (payment gateways, SMS/email providers, external APIs with
rate limits or costs) - without an `if (environment.equals("dev"))` check
anywhere in application logic.

## Key takeaways

- `application-<profile>.properties` files layer on top of the base
  `application.properties`, active only when that profile is active -
  values, not bean wiring.
- `@Profile("name")` on a `@Component`/`@Bean` controls whether that bean is
  created in the container **at all** - a mismatched or missing profile
  doesn't mean a default value or a null bean, it means the bean is never
  instantiated.
- If every implementation of an interface is profile-gated and no matching
  profile is active, dependency injection fails at startup with a real
  `NoSuchBeanDefinitionException`-style error - a safety net against
  accidentally deploying with the wrong (or no) profile, not a silent
  fallback to nothing.
- This is the standard, real pattern for swapping entire implementations
  per environment (dev/staging/prod) - fake or cheap stand-ins for external,
  paid, or dangerous dependencies in non-production profiles, the real thing
  gated behind the profile that's actually supposed to touch it.
