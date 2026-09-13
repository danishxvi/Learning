# Stage 1: extract the fat jar's layers (built by "mvn package" BEFORE this runs -
# see the .md for why building the jar happens outside Docker in this lesson).
#
# The jar and the extraction output must NOT share a directory - "extract" refuses to
# write into a non-empty destination, and /build already has application.jar in it by
# the time this runs. See the .md for the real error this produced on the first try.
FROM eclipse-temurin:21-jre-alpine AS builder
WORKDIR /build
COPY target/*.jar application.jar
RUN java -Djarmode=tools -jar application.jar extract --layers --launcher --destination /extracted

# Stage 2: the real runtime image - copies each layer in a DELIBERATE order, least
# likely to change first. Docker caches each COPY as its own layer; as long as a given
# layer's contents are byte-identical to the last build, Docker reuses the cached
# layer instead of re-copying it into the image.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /application
COPY --from=builder /extracted/dependencies/ ./
COPY --from=builder /extracted/spring-boot-loader/ ./
COPY --from=builder /extracted/snapshot-dependencies/ ./
COPY --from=builder /extracted/application/ ./
EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
