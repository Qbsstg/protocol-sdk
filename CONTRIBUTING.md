# Contributing

Thanks for your interest in Protocol SDK.

## Scope

This repository is focused on protocol parsing libraries. Runtime collectors,
network channel management, scheduling, persistence, message queues, and vendor
deployment configuration should live outside this repository.

## Development Requirements

- Keep source compatible with Java 8.
- The project may be built with newer JDKs, including JDK 21.
- Do not add Spring, Netty, database, Redis, RabbitMQ, or runtime global-state
  dependencies to parser modules.
- Prefer strongly typed protocol models over map-based APIs.
- Preserve raw bytes where useful for diagnostics and vendor-specific frames.

## Verification

Run tests before sending a change:

```bash
mvn -q test
```

For local Java 8 compatibility checks:

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-8.jdk/Contents/Home \
PATH=/Library/Java/JavaVirtualMachines/zulu-8.jdk/Contents/Home/bin:$PATH \
mvn -q test
```

## Adding Protocol Support

When adding a new IEC104 ASDU type, update these together:

- the enum/type definition
- the stream decoder
- the support matrix
- focused parser tests
- public README documentation
