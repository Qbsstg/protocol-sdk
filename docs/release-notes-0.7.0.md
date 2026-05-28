# Protocol SDK 0.7.0 Release Notes

`0.7.0` is the IEC104 hardening release for the Java 8 compatible Protocol SDK
line. The release keeps the SDK runtime-independent: parser modules do not
depend on Spring, Netty, databases, Redis, MQTT, Kafka, HTTP server frameworks,
schedulers, polling loops, retry policy, device registries, or collector
runtime globals.

## Highlights

- Refreshes the IEC104 completeness audit for the current post-`0.6.0`
  codebase.
- Documents IEC104 support as 45 typed Type IDs plus 8 recognized raw-only Type
  IDs, with unknown Type IDs preserved as raw bytes.
- Adds direct raw-byte preservation fixtures for `M_EI_NA_1` and file-transfer
  Type IDs `120` through `126`.
- Expands strict and permissive malformed-ASDU fixtures for truncated headers,
  information-object addresses, information elements, sequential elements, and
  raw-only catalog payloads.
- Adds VSQ/SQ boundary fixtures for representative typed and raw-only sequence
  and non-sequence layouts.
- Covers IEC104 cause-of-transmission diagnostic values, including
  counter-interrogation return causes and diagnostic unknown causes.
- Adds representative quality descriptor and `CP56Time2a` edge-case fixtures,
  including invalid dates, raw-byte preservation, summer-time flag, day-of-week
  bits, and boundary timestamp values.
- Expands IEC104 API documentation for decoder lifecycle, strict versus
  permissive parsing, raw fallback, support-matrix lookup, quality/time fields,
  and SDK/runtime boundaries.
- Keeps IEC101, IEC103, and Modbus in compatibility maintenance after their
  earlier published parser milestones.
- Keeps public source compatibility at Java 8 while CI verifies JDK 8 and JDK
  21.

## Maven Coordinates

Use protocol modules directly:

```xml
<dependency>
    <groupId>io.github.qbsstg</groupId>
    <artifactId>protocol-iec104</artifactId>
    <version>0.7.0</version>
</dependency>
```

Existing IEC101, IEC103, and Modbus users can continue to use their protocol
modules at the same reactor version:

```xml
<dependency>
    <groupId>io.github.qbsstg</groupId>
    <artifactId>protocol-iec101</artifactId>
    <version>0.7.0</version>
</dependency>
```

```xml
<dependency>
    <groupId>io.github.qbsstg</groupId>
    <artifactId>protocol-iec103</artifactId>
    <version>0.7.0</version>
</dependency>
```

```xml
<dependency>
    <groupId>io.github.qbsstg</groupId>
    <artifactId>protocol-modbus</artifactId>
    <version>0.7.0</version>
</dependency>
```

Most applications should not depend on the parent `protocol-sdk` POM directly.

## Module Status

| Module | Status in `0.7.0` |
| --- | --- |
| `protocol-core` | Stable shared parser contracts. |
| `protocol-iec104` | Hardened IEC104 parser surface with current audit, fixtures, and API behavior docs. |
| `protocol-iec101` | Published IEC101 parser, compatibility-maintained after `0.5.0`. |
| `protocol-iec103` | Published IEC103 parser, compatibility-maintained after `0.5.0`. |
| `protocol-modbus` | Stable Modbus TCP/UDP parser, compatibility-maintained after `0.6.0`. |

## IEC104 Scope

The `0.7.0` IEC104 scope is parser-only:

- APDU stream decoding for I-format, S-format, and U-format frames.
- Typed ASDU values for the practical IEC104 process, command, counter,
  clock, reset, delay acquisition, and parameter families already listed in the
  support matrix.
- Recognized raw-only handling for end-of-initialization and file-transfer Type
  IDs.
- Raw fallback for unknown Type IDs and vendor-specific payloads.
- Strict optional diagnostics for truncated recognized typed ASDUs.
- Raw APDU, ASDU, information-object, and time-tag bytes preserved for
  diagnostics.

Out of scope:

- Formal IEC 60870-5-104 conformance certification.
- IEC104 TCP session state machines, STARTDT/STOPDT lifecycle policy, TESTFR
  heartbeat policy, reconnects, polling loops, retry policy, command routing,
  select-before-operate workflow, telemetry storage, or runtime ingestion.
- Netty, Spring, MQTT, Kafka, HTTP server/client, database, Redis, device
  registry, or collector runtime dependencies inside SDK modules.
- Promoting IEC104 initialization or file-transfer Type IDs to typed models
  without real device traces or a separate design decision.

## IEC104 Documentation

- Support matrix:
  [`protocol-iec104/docs/asdu-support-matrix.md`](../protocol-iec104/docs/asdu-support-matrix.md)
- API usage guide:
  [`protocol-iec104/docs/api-usage.md`](../protocol-iec104/docs/api-usage.md)
- Completeness audit:
  [`protocol-iec104/docs/iec104-completeness-audit.md`](../protocol-iec104/docs/iec104-completeness-audit.md)

## Verification

The `0.7.0` release should be tagged only after these checks pass on the final
release commit:

- `mvn -q -pl protocol-iec104 -am test`
- `mvn -q verify`
- JDK 8 `mvn -q verify`
- GitHub Actions on JDK 8 and JDK 21
- Central release profile smoke check with publishing disabled
- signed dry run with `central.skipPublishing=true`

After publication, verify Maven Central availability with an isolated temporary
consumer before announcing the release.
