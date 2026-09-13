# 61 - Centralized Configuration with Config Server

Every service in this curriculum so far has kept its own
`application.properties` bundled inside its own jar - fine for one service,
unworkable for a fleet of them. Changing a shared setting (a feature flag, a
timeout, a downstream URL) across ten services means rebuilding and
redeploying all ten. **Spring Cloud Config** centralizes configuration in one
place: services fetch their config from a Config Server at startup, and can
be told to re-fetch it live, without a restart, when it changes.

Two separate runnable Maven projects:

```
61-centralized-configuration-with-config-server/
├── config-server/   - serves configuration from a local directory
└── config-client/   - fetches its config from the server at startup
```

## The server: backed by a plain local directory

[`ConfigServerApplication.java`](config-server/src/main/java/com/danish/spring/configserver/ConfigServerApplication.java):

```java
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
```

```properties
server.port=8888
spring.profiles.active=native
spring.cloud.config.server.native.search-locations=file:./config-repo
```

Config Server usually serves from a git repository (the classic setup: a
`git pull` per fetch, `git log` as a full audit trail of every config
change). This lesson uses the simpler **native** backend - a plain local
directory - specifically because it's a real directory on disk that can be
edited *while the server is running*, which the live-refresh demo below
depends on. [`config-repo/`](config-server/config-repo/) holds three files:

```properties
# application.properties - shared defaults for EVERY client
greeting.message=Hello from the shared default config (application.properties)
```
```properties
# config-client.properties - specific to the app named "config-client"
greeting.message=Hello from config-client's own config file (config-client.properties)
```
```properties
# config-client-dev.properties - only applies when config-client's "dev" profile is active
greeting.message=Hello from config-client's DEV profile config (config-client-dev.properties)
```

```bash
mvn -f Spring/12-microservices-with-spring-cloud/61-centralized-configuration-with-config-server/config-server spring-boot:run
```

Config Server exposes a REST API of its own - `/{application}/{profile}` -
independent of any client. Real output, queried directly with `curl`:

```bash
curl -s http://localhost:8888/config-client/default
```
```json
{"name":"config-client","profiles":["default"],
 "propertySources":[
   {"name":"file:config-repo/config-client.properties",
    "source":{"greeting.message":"Hello from config-client's own config file (config-client.properties)"}},
   {"name":"file:config-repo/application.properties",
    "source":{"greeting.message":"Hello from the shared default config (application.properties)"}}
 ]}
```

```bash
curl -s http://localhost:8888/config-client/dev
```
```json
{"name":"config-client","profiles":["dev"],
 "propertySources":[
   {"name":"file:config-repo/config-client-dev.properties",
    "source":{"greeting.message":"Hello from config-client's DEV profile config (config-client-dev.properties)"}},
   {"name":"file:config-repo/config-client.properties",
    "source":{"greeting.message":"Hello from config-client's own config file (config-client.properties)"}},
   {"name":"file:config-repo/application.properties",
    "source":{"greeting.message":"Hello from the shared default config (application.properties)"}}
 ]}
```

Real, observable **precedence**: `propertySources` is ordered most-specific
first. With no profile active, `config-client.properties` (app-specific)
appears before `application.properties` (shared default) - the app-specific
value wins. With the `dev` profile active, `config-client-dev.properties`
appears *before even that* - the profile-specific value wins over both.

## The client: fetches its config at startup

[`application.properties`](config-client/src/main/resources/application.properties):

```properties
spring.application.name=config-client
spring.config.import=configserver:http://localhost:8888
```

`spring.config.import=configserver:...` is the whole integration - Spring
Boot fetches this application's config from that URL, using
`spring.application.name` (and any active profile) to select which files
apply, before the rest of the application even starts. [`GreetingController.java`](config-client/src/main/java/com/danish/spring/configclient/GreetingController.java)
just reads the result:

```java
@Value("${greeting.message}")
private String greetingMessage;

@GetMapping("/greeting")
public String greeting() { return greetingMessage; }
```

With `config-server` already running, real output:

```bash
mvn -f Spring/12-microservices-with-spring-cloud/61-centralized-configuration-with-config-server/config-client spring-boot:run
```
```bash
curl -s http://localhost:8090/greeting
```
```
Hello from config-client's own config file (config-client.properties)
```

Restarted with the `dev` profile active:

```bash
mvn -f .../config-client spring-boot:run -Dspring-boot.run.profiles=dev
```
```bash
curl -s http://localhost:8090/greeting
```
```
Hello from config-client's DEV profile config (config-client-dev.properties)
```

Exactly matching what the server's own API reported above - the client
genuinely fetched and applied the same precedence.

## Live refresh: changing config without restarting anything

`@Value` normally binds once, at startup, and never changes again for the
life of the process - that's true everywhere else in this curriculum.
`@RefreshScope` on `GreetingController` makes it special: the bean can be
**recreated** on demand, re-reading its `@Value` fields, when told to.

With `config-server` and `config-client` both still running (no profile,
back on the base `config-client.properties` value), the config file was
edited directly on disk:

```properties
# config-repo/config-client.properties, edited live
greeting.message=UPDATED: config-client's own config file, edited while everything was still running
```

Real sequence, in order:

```bash
curl -s http://localhost:8090/greeting
```
```
Hello from config-client's own config file (config-client.properties)
```
_(still the OLD value - editing the file alone changed nothing on the running client)_

```bash
curl -s http://localhost:8888/config-client/default
```
```json
{"propertySources":[{"source":{"greeting.message":"UPDATED: config-client's own config file, edited while everything was still running"}}, ...]}
```
_(the SERVER already sees the new value - native backend re-reads the file on every request)_

```bash
curl -s -X POST http://localhost:8090/actuator/refresh
```
```json
["greeting.message"]
```
_(the client explicitly asks the server for fresh config and reports which properties changed)_

```bash
curl -s http://localhost:8090/greeting
```
```
UPDATED: config-client's own config file, edited while everything was still running
```

No restart, no redeploy - the running JVM picked up new configuration
because `@RefreshScope` let Spring throw away and recreate that one bean.
Without `@RefreshScope` on `GreetingController`, the `@Value` binding would
have been fixed at the original startup and `/actuator/refresh` would have
had nothing to actually change on this particular bean.

## Key takeaways

- `@EnableConfigServer` plus a backend (git, or the simpler native local
  directory used here) turns a Spring Boot app into a configuration
  registry, queryable directly via its own REST API
  (`/{application}/{profile}`).
- `spring.config.import=configserver:<url>` is all a client needs to fetch
  its configuration from the server at startup, resolved by
  `spring.application.name` and active profile.
- Precedence is real and observable: profile-specific config
  (`<app>-<profile>.properties`) beats app-specific config
  (`<app>.properties`), which beats the shared default
  (`application.properties`) - confirmed both from the server's own API and
  from the client's actually-applied value.
- `@RefreshScope` is what makes a bean's `@Value` fields eligible to be
  re-read after a `POST /actuator/refresh` - without it, config fetched at
  startup is permanent for that bean's lifetime, refresh or not.
- The native filesystem backend re-reads the directory on every request with
  no caching of its own, which is what allowed a plain file edit, with no
  commit or restart of anything, to be visible to the server immediately -
  and then to the client, once explicitly refreshed.
