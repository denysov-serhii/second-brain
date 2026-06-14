# syntax=docker/dockerfile:1.7

# -----------------------------------------------------------------------------
# Stage 1: build native executable with GraalVM + Maven
# -----------------------------------------------------------------------------
FROM ghcr.io/graalvm/native-image-community:25 AS native-builder

WORKDIR /workspace

# Install Maven in the build image.
RUN if command -v microdnf >/dev/null 2>&1; then \
      microdnf install -y maven findutils && microdnf clean all; \
    elif command -v dnf >/dev/null 2>&1; then \
      dnf install -y maven findutils && dnf clean all; \
    elif command -v apt-get >/dev/null 2>&1; then \
      apt-get update && apt-get install -y maven findutils && rm -rf /var/lib/apt/lists/*; \
    else \
      echo "No supported package manager found to install Maven" && exit 1; \
    fi

# Copy only the Maven descriptor first to maximize dependency layer caching.
COPY server/pom.xml server/pom.xml
RUN mvn -f server/pom.xml -B -ntp dependency:go-offline

# Copy application sources and compile the native binary.
COPY server/src server/src
RUN mvn -f server/pom.xml -B -ntp -DskipTests native:compile-no-fork

# -----------------------------------------------------------------------------
# Stage 2: minimal runtime image (distroless, non-root)
# -----------------------------------------------------------------------------
FROM gcr.io/distroless/base-debian12:nonroot

WORKDIR /app

# Copy only the native binary. No shell/package manager in final image.
COPY --from=native-builder /workspace/server/target/second-brain-server /app/second-brain-server

EXPOSE 8080

ENTRYPOINT ["/app/second-brain-server"]
