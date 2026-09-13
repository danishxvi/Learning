# 68 - Dockerizing a Spring Boot Application

Lesson 67 built a real fat jar and, separately, extracted its layers to show
that "our code" (21KB) and "the dependencies" (19MB) are wildly different in
both size and how often they change. This lesson puts that split to actual
use: a real multi-stage `Dockerfile` that copies those layers into an image
in an order chosen specifically so Docker's own build cache can skip
re-copying the huge, rarely-changing parts on every rebuild. Everything
below is a real `docker build` and `docker run`, not a description of what
should theoretically happen.

```bash
mvn -f Spring/13-production-readiness/68-dockerizing-a-spring-boot-application package
docker build -t learning-dockerized-demo Spring/13-production-readiness/68-dockerizing-a-spring-boot-application
docker run -p 8080:8080 learning-dockerized-demo
```

## The Dockerfile: layers copied in a deliberate order

[`Dockerfile`](Dockerfile):

```dockerfile
FROM eclipse-temurin:21-jre-alpine AS builder
WORKDIR /build
COPY target/*.jar application.jar
RUN java -Djarmode=tools -jar application.jar extract --layers --launcher --destination /extracted

FROM eclipse-temurin:21-jre-alpine
WORKDIR /application
COPY --from=builder /extracted/dependencies/ ./
COPY --from=builder /extracted/spring-boot-loader/ ./
COPY --from=builder /extracted/snapshot-dependencies/ ./
COPY --from=builder /extracted/application/ ./
EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
```

Two stages: the first (`builder`) only exists to run the layer extraction
tool from lesson 67 inside a container; its output is copied out and it's
discarded from the final image entirely (multi-stage builds keep the final
image free of build-only tooling). The second, real runtime stage copies
each layer with its **own** `COPY` instruction, in order from *least* to
*most* likely to change: `dependencies` (third-party jars - barely ever
changes for a given app), `spring-boot-loader` (Spring Boot's own launcher
classes - changes only on a Spring Boot version bump), `snapshot-dependencies`
(any `-SNAPSHOT` deps, empty here), then `application` (this project's own
code - changes on every single build).

Docker caches each instruction as its own image layer, keyed by its inputs.
If a layer's inputs are byte-identical to a previous build, Docker reuses
the cached result instead of redoing the work - and because these four
`COPY`s each touch a *different* subset of files, changing only the
application's own code only invalidates the cache for the *last* `COPY`,
not the ones before it.

## A real gotcha hit building this: `extract` refuses a non-empty destination

The first version of this Dockerfile had the extraction destination be the
*same* directory the jar was copied into:

```dockerfile
WORKDIR /extracted
COPY target/*.jar application.jar
RUN java -Djarmode=tools -jar application.jar extract --layers --launcher --destination .
```

Building it failed immediately:

```
Error: /extracted/. already exists and is not empty
```

**Why**: by the time `extract` runs, `/extracted` already contains
`application.jar` (from the `COPY` right before it) - the tool refuses to
extract into a directory that isn't empty, presumably to avoid silently
mixing extracted layer contents with whatever else happens to already be
there. The fix was separating the two concerns into different directories -
`/build` holds the jar, `/extracted` (created fresh by the tool itself) holds
the extraction output:

```dockerfile
WORKDIR /build
COPY target/*.jar application.jar
RUN java -Djarmode=tools -jar application.jar extract --layers --launcher --destination /extracted
```

With that change, the build succeeded.

## Real proof it works: a running container, answering real HTTP requests

```bash
docker run -d --name learning-dockerized-demo -p 8080:8080 learning-dockerized-demo
curl -s http://localhost:8080/ping
```
```
pong from inside a Docker container - version 1
```

```bash
docker logs learning-dockerized-demo
```
```
Tomcat started on port 8080 (http) with context path '/'
Started DockerizedApplication in 2.793 seconds (process running for 3.456)
```

A genuine Spring Boot application, started by `JarLauncher` running inside
an Alpine-based JRE container, serving a real HTTP response on the mapped
port - the exact same jar and the exact same startup sequence as lesson 67,
just inside a container instead of directly on the host.

## Real proof the layer split does something: only the changed layer rebuilds

One line of application code was changed (the `/ping` response text), the
jar was rebuilt with `mvn package`, and the image was rebuilt with the exact
same `docker build` command. Real build output:

```
#6 [builder 2/4] WORKDIR /build
#6 CACHED

#9 [stage-1 3/6] COPY --from=builder /extracted/dependencies/ ./
#9 CACHED

#10 [stage-1 4/6] COPY --from=builder /extracted/spring-boot-loader/ ./
#10 CACHED

#12 [stage-1 5/6] COPY --from=builder /extracted/snapshot-dependencies/ ./
#12 CACHED

#13 [stage-1 6/6] COPY --from=builder /extracted/application/ ./
#13 DONE 0.1s
```

Three of the four layer-copying steps show `CACHED` - Docker recognized
their inputs hadn't changed and skipped redoing them entirely. Only the
`application` layer's `COPY` actually ran again, because that's the only
one whose input (this project's own compiled code) genuinely changed. The
rebuilt container, confirmed running the new code:

```bash
curl -s http://localhost:8080/ping
```
```
pong from inside a Docker container - version 2 (only this line changed)
```

In a real CI/CD pipeline pushing images to a registry, this caching is what
makes routine deploys fast and cheap: only the small, changed layer needs
to be rebuilt, re-uploaded, and re-pulled by whatever's running the
container - the 19MB of dependencies (lesson 67's own measurement) is
untouched, both in the build and in the network transfer.

## Key takeaways

- A multi-stage `Dockerfile` can use one stage purely to extract or prepare
  files (here, running lesson 67's `--layers` extraction) and discard that
  stage entirely from the final image - only what's explicitly `COPY
  --from`'d survives into the runtime image.
- `java -Djarmode=tools -jar app.jar extract` refuses to write into a
  non-empty destination directory - keep the source jar and the extraction
  output in separate directories.
- Copying each Spring Boot layer with its own `COPY` instruction, ordered
  least-to-most likely to change, lets Docker's build cache skip re-copying
  layers whose contents haven't changed - verified here directly from real
  `docker build` output showing `CACHED` on three of four layers after an
  application-only code change.
- The resulting image runs the exact same `JarLauncher`-based startup as
  running the fat jar directly (lesson 67) - Docker doesn't change how the
  application boots, only how it's packaged, shipped, and cached.
