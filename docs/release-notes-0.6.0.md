# Protocol SDK 0.6.0 Release Notes

`0.6.0` is the Modbus stable-completion release for the Java 8 compatible
Protocol SDK line. The release keeps the SDK runtime-independent: parser
modules do not depend on Spring, Netty, databases, Redis, MQTT, Kafka, HTTP
server frameworks, schedulers, polling loops, retry policy, device registries,
or collector runtime globals.

## Highlights

- Promotes `protocol-modbus` from the `0.5.0` experimental baseline to a stable
  Modbus TCP and common Modbus-over-UDP parser module.
- Documents Modbus function-code support with typed, raw-only, unknown, and
  deferred behavior.
- Adds usage guidance for TCP stream decoding, UDP datagram decoding,
  request/response correlation, exception responses, and raw fallback.
- Promotes read/write multiple registers (`0x17`) from raw-only to typed
  request and response parsing.
- Hardens malformed MBAP/PDU behavior for TCP and UDP parser paths.
- Adds standard quantity-limit, byte-count, register-byte-count, and coil-value
  validation coverage for typed Modbus function codes.
- Locks public Modbus model immutability expectations with final model classes,
  private final fields, and defensive-copy tests for array-backed values.
- Keeps IEC101, IEC103, and IEC104 in compatibility maintenance after their
  earlier published parser milestones.
- Keeps public source compatibility at Java 8 while CI verifies JDK 8 and JDK
  21.

## Maven Coordinates

Use protocol modules directly:

```xml
<dependency>
    <groupId>io.github.qbsstg</groupId>
    <artifactId>protocol-modbus</artifactId>
    <version>0.6.0</version>
</dependency>
```

Existing IEC users can continue to use their protocol modules at the same
reactor version:

```xml
<dependency>
    <groupId>io.github.qbsstg</groupId>
    <artifactId>protocol-iec104</artifactId>
    <version>0.6.0</version>
</dependency>
```

```xml
<dependency>
    <groupId>io.github.qbsstg</groupId>
    <artifactId>protocol-iec101</artifactId>
    <version>0.6.0</version>
</dependency>
```

```xml
<dependency>
    <groupId>io.github.qbsstg</groupId>
    <artifactId>protocol-iec103</artifactId>
    <version>0.6.0</version>
</dependency>
```

Most applications should not depend on the parent `protocol-sdk` POM directly.

## Module Status

| Module | Status in `0.6.0` |
| --- | --- |
| `protocol-core` | Stable shared parser contracts. |
| `protocol-iec104` | Stable IEC104 parser surface, compatibility-maintained. |
| `protocol-iec101` | Published IEC101 parser, compatibility-maintained after `0.5.0`. |
| `protocol-iec103` | Published IEC103 parser, compatibility-maintained after `0.5.0`. |
| `protocol-modbus` | Stable parser module for practical Modbus TCP/UDP parser use. |

`protocol-modbus` is stable for the documented parser-only TCP/UDP MBAP ADU/PDU
surface. It does not claim Modbus RTU/ASCII serial framing, socket clients,
polling, request scheduling, telemetry storage, or runtime ingestion support.

## Modbus Scope

The `0.6.0` Modbus scope is parser-only:

- Modbus TCP MBAP ADU stream decoding.
- Modbus-over-UDP MBAP datagram decoding.
- Typed PDU values for common process-data function codes.
- Typed Modbus exception responses.
- Raw fallback for unknown, vendor-specific, or intentionally deferred function
  codes.
- Raw ADU, PDU, and value bytes preserved for diagnostics.

Out of scope:

- Modbus RTU and Modbus ASCII serial framing.
- Socket clients, connection pools, polling loops, retry policy, request
  scheduling, and timeout management.
- Device registry, point mapping, telemetry storage, alarm rules, or runtime
  ingestion adapters.
- Netty, Spring, MQTT, Kafka, HTTP server, database, Redis, or collector
  runtime dependencies inside SDK modules.

## Function-Code Coverage

The typed coverage for `0.6.0` is:

| Code | Name | Target support |
| --- | --- | --- |
| `0x01` | Read coils | Typed request and response. |
| `0x02` | Read discrete inputs | Typed request and response. |
| `0x03` | Read holding registers | Typed request and response. |
| `0x04` | Read input registers | Typed request and response. |
| `0x05` | Write single coil | Typed request and response echo. |
| `0x06` | Write single register | Typed request and response echo. |
| `0x0F` | Write multiple coils | Typed request and response. |
| `0x10` | Write multiple registers | Typed request and response. |
| `0x17` | Read/write multiple registers | Typed request and response. |
| `0x80`-style responses | Exception responses | Typed exception response with original function code and exception code. |
| Unknown/vendor codes | Vendor-specific payloads | Raw fallback when the ADU/PDU envelope is valid. |

The detailed support matrix is maintained in
[`protocol-modbus/docs/function-support-matrix.md`](../protocol-modbus/docs/function-support-matrix.md).

## Verification

The `0.6.0` release was tagged after these checks passed on the merged release
commit:

- `mvn -q -pl protocol-modbus -am test`
- `mvn -q verify`
- JDK 8 `mvn -q verify`
- GitHub Actions on JDK 8 and JDK 21
- Central release profile smoke check with publishing disabled
- signed dry run with `central.skipPublishing=true`

After publication, an isolated temporary Maven consumer resolved and compiled
against the `0.6.0` artifacts from Maven Central.
