# Protocol SDK 0.5.0 Release Notes

`0.5.0` is the IEC101 and IEC103 completion release for the Java 8 compatible
Protocol SDK line. The release keeps the SDK runtime-independent: parser modules
do not depend on Spring, Netty, databases, Redis, MQTT, Kafka, HTTP server
frameworks, schedulers, or collector runtime globals.

## Highlights

- Adds a practical IEC101 parser completion target with FT1.2 frames, typed
  process values, time-tagged process values, common station-service commands,
  configurable address lengths, and raw-byte fallback for unknown Type IDs.
- Adds a practical IEC103 parser completion target with FT1.2 frames, typed
  protection events, typed measurands, raw-backed identification values,
  raw-only Type ID 4 behavior, and unknown/deferred raw-byte fallback.
- Keeps IEC104 compatibility with the existing published parser surface.
- Publishes Modbus with the same reactor version as an experimental module;
  Modbus stable completion is deferred to the next SDK phase and does not block
  `0.5.0`.
- Keeps public source compatibility at Java 8 while CI verifies JDK 8 and JDK 21.

## Maven Coordinates

Use protocol modules directly:

```xml
<dependency>
    <groupId>io.github.qbsstg</groupId>
    <artifactId>protocol-iec101</artifactId>
    <version>0.5.0</version>
</dependency>
```

```xml
<dependency>
    <groupId>io.github.qbsstg</groupId>
    <artifactId>protocol-iec103</artifactId>
    <version>0.5.0</version>
</dependency>
```

Existing IEC104 users can use:

```xml
<dependency>
    <groupId>io.github.qbsstg</groupId>
    <artifactId>protocol-iec104</artifactId>
    <version>0.5.0</version>
</dependency>
```

Experimental Modbus users can use:

```xml
<dependency>
    <groupId>io.github.qbsstg</groupId>
    <artifactId>protocol-modbus</artifactId>
    <version>0.5.0</version>
</dependency>
```

Most applications should not depend on the parent `protocol-sdk` POM directly.

## Module Status

| Module | Status in `0.5.0` |
| --- | --- |
| `protocol-core` | Stable shared parser contracts. |
| `protocol-iec104` | Stable IEC104 parser surface, compatibility-maintained. |
| `protocol-iec101` | `0.5.0` completion target for practical IEC101 parser use. |
| `protocol-iec103` | `0.5.0` completion target for practical IEC103 relay parser use. |
| `protocol-modbus` | Experimental parser module, published but non-blocking for `0.5.0`. |

## Verification

Release candidate verification has passed locally:

- `mvn -q verify`
- JDK 8 `mvn -q verify`
- Central release profile smoke check with publishing disabled
- signed dry run with `central.skipPublishing=true`

The release PR must still pass GitHub Actions on JDK 8 and JDK 21 before tagging
and publishing.
