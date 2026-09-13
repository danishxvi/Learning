# 67 - Packaging and Running the Fat Jar

Every lesson so far has been run with `mvn spring-boot:run` - Maven compiles
and starts the app in one step, inside the build tool. A real deployment
doesn't have Maven installed on the server; it has a single artifact copied
somewhere and run with `java -jar`. This lesson builds that artifact for
real, inspects exactly what's inside it, and runs it completely
independently of Maven, the IDE, or this project's `target/classes`.

```bash
mvn -f Spring/13-production-readiness/67-packaging-and-running-the-fat-jar package
```

## `mvn package` produces TWO jars

```bash
ls -la target/*.jar*
```
```
67-packaging-and-running-the-fat-jar-1.0.0.jar            20033715 bytes
67-packaging-and-running-the-fat-jar-1.0.0.jar.original        3901 bytes
```

Real, and easy to miss the first time: Maven's own `jar` plugin produces the
**thin** jar first (`.jar`, containing only this project's own compiled
classes - ~4KB, all of which is `FatJarApplication.class` and its manifest).
`spring-boot-maven-plugin`'s `repackage` goal then **replaces** the main
`.jar` artifact with the executable "fat" jar, and renames the original thin
jar to `.jar.original` so it isn't lost. The ~20MB file is what actually gets
deployed and run - the ~4KB one is an intermediate build artifact, not
runnable on its own (it has no dependencies and no embedded launcher).

## What's actually inside the fat jar

```bash
unzip -p target/67-packaging-and-running-the-fat-jar-1.0.0.jar META-INF/MANIFEST.MF
```
```
Main-Class: org.springframework.boot.loader.launch.JarLauncher
Start-Class: com.danish.spring.fatjar.FatJarApplication
Spring-Boot-Classes: BOOT-INF/classes/
Spring-Boot-Lib: BOOT-INF/lib/
Spring-Boot-Layers-Index: BOOT-INF/layers.idx
```

`Main-Class` is **not** this project's own class - it's Spring Boot's own
`JarLauncher`, bundled inside the jar. `Start-Class` is where the real
`main()` actually lives; `JarLauncher` reads that from the manifest, sets up
a classloader that can load classes and jars nested *inside* this single
jar file (a standard `java.util.zip` reader can't do that on its own - nested
jars aren't a JVM classpath concept), and only then invokes it.

Looking inside confirms the layout the manifest describes:

```bash
unzip -l target/67-packaging-and-running-the-fat-jar-1.0.0.jar | grep "BOOT-INF/classes/com/danish"
```
```
BOOT-INF/classes/com/danish/spring/fatjar/FatJarApplication$PingController.class
BOOT-INF/classes/com/danish/spring/fatjar/FatJarApplication.class
```

```bash
unzip -l target/67-packaging-and-running-the-fat-jar-1.0.0.jar | grep "BOOT-INF/lib/spring-webmvc"
```
```
1047770  BOOT-INF/lib/spring-webmvc-6.1.13.jar
```

This project's own two tiny classes live under `BOOT-INF/classes/`, exactly
matching the thin jar's contents. Every dependency - including
`spring-webmvc`, at over a megabyte on its own - is bundled **whole**, as its
own untouched jar file, under `BOOT-INF/lib/`. This is what "fat jar" means
literally: one file containing this application's code plus every
dependency it needs, in a structure only Spring Boot's own loader knows how
to execute directly.

## Running it: genuinely self-contained

The jar was copied conceptually elsewhere by simply running it from an
entirely unrelated directory (`/tmp`, nothing to do with this Maven
project), with only `java -jar`:

```bash
java -jar /path/to/67-packaging-and-running-the-fat-jar-1.0.0.jar
```
```bash
curl -s http://localhost:8080/ping
```
```
pong from the fat jar, running with no IDE, no mvn, no target/classes on the classpath
```

No `mvn`, no project directory, no manually-assembled classpath - the jar
carried everything it needed. This is the actual artifact a real deployment
ships: copy this one file to a server (or into a container, lesson 68) with
a JDK installed, and `java -jar app.jar` is the entire deployment.

## Layered jars: separating "our code" from "the dependencies"

`<layers><enabled>true</enabled></layers>` on the Spring Boot Maven plugin
(configured in this lesson's [`pom.xml`](pom.xml)) adds a
`BOOT-INF/layers.idx` describing how to split the jar's contents into
layers by how often they change. Real output:

```bash
java -Djarmode=tools -jar target/67-packaging-and-running-the-fat-jar-1.0.0.jar list-layers
```
```
dependencies
spring-boot-loader
snapshot-dependencies
application
```

Extracting them (`... extract --layers --launcher`) into real, separate
directories and measuring each:

```bash
du -sh dependencies application
```
```
19M   dependencies
21K   application
```

`dependencies` - every third-party jar this application needs (Spring
itself, Tomcat, Jackson, ...) - is 19MB and, realistically, barely changes
between builds of the *same* application. `application` - this project's
own compiled classes and resources - is 21KB and changes on **every**
build. Splitting these into separate layers is exactly what makes Docker
layer caching effective (lesson 68): a container image can cache the 19MB
`dependencies` layer indefinitely and only ever need to rebuild the tiny
21KB `application` layer when the actual code changes, instead of
re-uploading and re-extracting the whole 20MB fat jar on every single
deploy.

## Key takeaways

- `mvn package` with the Spring Boot Maven plugin produces two jars: a thin
  `.jar.original` (this project's classes only, not runnable standalone)
  and the real, executable fat `.jar` that replaces it as the main artifact.
- The fat jar's `Main-Class` is Spring Boot's own `JarLauncher`, not the
  application's class - it reads `Start-Class` from the manifest and
  provides the classloader needed to load classes and jars nested *inside*
  the outer jar, something a plain JVM classpath can't do on its own.
- This project's own compiled code lives under `BOOT-INF/classes/`; every
  dependency is bundled as its own complete, untouched jar file under
  `BOOT-INF/lib/` - the whole point of "fat" is that nothing external is
  needed at runtime beyond a JDK.
- A fat jar genuinely runs anywhere with `java -jar`, independent of Maven,
  an IDE, or the source project's directory structure - confirmed here by
  running it from a completely unrelated directory.
- Enabling layered jars splits the archive into layers by change frequency
  (`dependencies`, `application`, etc.) - measured here at 19MB of rarely-
  changing dependencies versus 21KB of code that changes on every build,
  which is precisely what makes container image layer caching effective for
  a Spring Boot application.
