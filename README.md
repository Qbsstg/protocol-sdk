# Protocol SDK

Protocol SDK is a standalone Java protocol parsing SDK. It is split from a
legacy collector runtime and intentionally keeps only protocol parsing,
streaming decoders, and strongly typed protocol models.

The first public baseline started with IEC 60870-5-104, and the current
published SDK line also includes IEC101, IEC103, and stable Modbus TCP/UDP
parser modules. The SDK does not depend on Spring, Netty, databases, Redis,
message queues, or collector runtime globals.

## Maven Central

Current stable release: `0.6.0`

Use protocol modules directly in applications. For Modbus TCP/UDP parsing:

```xml
<dependency>
    <groupId>io.github.qbsstg</groupId>
    <artifactId>protocol-modbus</artifactId>
    <version>0.6.0</version>
</dependency>
```

Existing IEC users can use the same reactor version:

```xml
<dependency>
    <groupId>io.github.qbsstg</groupId>
    <artifactId>protocol-iec104</artifactId>
    <version>0.6.0</version>
</dependency>
```

The shared core contracts are published separately and are pulled transitively
by protocol modules:

```xml
<dependency>
    <groupId>io.github.qbsstg</groupId>
    <artifactId>protocol-core</artifactId>
    <version>0.6.0</version>
</dependency>
```

The parent `protocol-sdk` POM is for building this repository. Most users should
depend on protocol modules such as `protocol-modbus` or `protocol-iec104`.

## Quick Usage

The decoder is stream-oriented. It buffers incomplete APDUs internally, so
callers can feed bytes directly from TCP packets without pre-splitting frames.

```java
import io.github.qbsstg.protocol.core.ParseResult;
import io.github.qbsstg.protocol.iec104.Iec104Frame;
import io.github.qbsstg.protocol.iec104.Iec104InformationObject;
import io.github.qbsstg.protocol.iec104.Iec104SinglePointValue;
import io.github.qbsstg.protocol.iec104.Iec104StreamDecoder;

import java.util.List;

public final class Iec104Example {
    public static void main(String[] args) {
        Iec104StreamDecoder decoder = new Iec104StreamDecoder();

        List<ParseResult<Iec104Frame>> results = decoder.decode(bytes(
                0x68, 0x15,
                0x00, 0x00, 0x00, 0x00,
                0x1E, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x01, 0x00, 0x00,
                0x01, 0xE8, 0x03, 0x15, 0x10, 0x15, 0x05, 0x1A));

        ParseResult<Iec104Frame> result = results.get(0);
        if (!result.isSuccess()) {
            throw new IllegalStateException(result.getMessage());
        }

        Iec104Frame frame = result.getFrame();
        Iec104InformationObject object = frame.getAsdu().getInformationObjects().get(0);
        Iec104SinglePointValue value = (Iec104SinglePointValue) object.getValue();

        System.out.println("type=" + frame.getAsdu().getType());
        System.out.println("address=" + object.getAddress());
        System.out.println("on=" + value.isOn());
        System.out.println("time=" + value.getTimeTag().getDateTime());
    }

    private static byte[] bytes(int... values) {
        byte[] bytes = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            bytes[i] = (byte) (values[i] & 0xFF);
        }
        return bytes;
    }
}
```

For more examples, see
[`Iec104SdkUsageExampleTest`](protocol-iec104/src/test/java/io/github/qbsstg/protocol/iec104/Iec104SdkUsageExampleTest.java)
and the
[`IEC104 API Usage Guide`](protocol-iec104/docs/api-usage.md).

## Modules

| Module | Status | Purpose |
| --- | --- | --- |
| `protocol-core` | Published | Shared Java 8 compatible parser contracts and result types. |
| `protocol-iec104` | Published | IEC 60870-5-104 APDU, ASDU, information object, and typed value parser. |
| `protocol-iec101` | Published | IEC 60870-5-101 FT1.2 frame parser and practical ASDU completion target. [Support matrix](protocol-iec101/docs/asdu-support-matrix.md). |
| `protocol-iec103` | Published | IEC 60870-5-103 FT1.2 frame parser with protection event, measurand, identification, and raw fallback completion target. [Support matrix](protocol-iec103/docs/asdu-support-matrix.md), [Design note](docs/iec103-module-design.md), [Usage](protocol-iec103/docs/api-usage.md). |
| `protocol-modbus` | `0.6.0` stable | Modbus TCP/UDP ADU, PDU, typed value, and exception parser. [Support matrix](protocol-modbus/docs/function-support-matrix.md), [Usage](protocol-modbus/docs/api-usage.md), [Design note](docs/protocol-modbus-design.md). |
| `protocol-http` | Planned | Design pending; HTTP ingestion and server/client concerns remain runtime work unless a small Java 8 SDK helper surface is justified. |

`0.6.0` promotes `protocol-modbus` to a stable parser module for practical
Modbus TCP and common Modbus-over-UDP MBAP ADU/PDU parsing. The scope remains
parser-only; serial RTU/ASCII framing, socket clients, polling, retry policy,
device registries, and ingestion adapters belong outside this SDK.

## IEC104 Coverage

`protocol-iec104` currently recognizes 53 ASDU types: 45 typed Type IDs and 8
recognized raw-only Type IDs. Unknown Type IDs are preserved as raw bytes for
diagnostics and vendor-specific handling.

Typed coverage includes:

- Single point, double point, step position, bitstring, measured values, and
  integrated totals.
- Time-tagged variants with `CP56Time2a`.
- Packed single-point information and protection event groups.
- Single, double, regulating step, set point, bitstring, interrogation, counter
  interrogation, read, clock synchronization, reset process, and delay
  acquisition commands.
- Parameter measured values and parameter activation.

The detailed matrix is maintained in
[`protocol-iec104/docs/asdu-support-matrix.md`](protocol-iec104/docs/asdu-support-matrix.md).
`M_EI_NA_1` and file-transfer Type IDs `120` through `126` are intentionally
raw-only until real device traces justify stable typed public models.

## API Shape

- `ByteStreamDecoder<T>`: streaming parser contract.
- `ParseResult<T>`: success, incomplete, or error result.
- `Iec104Frame`: APDU frame type and send/receive sequence numbers.
- `Iec104Asdu`: ASDU header, cause of transmission, common address, and objects.
- `Iec104InformationObject`: information object address, raw bytes, typed value.
- `Iec104InformationValue`: marker interface for typed IEC104 values.
- `Iec104AsduSupport`: typed/raw-only/unknown support status for ASDU types.

Raw bytes remain available on frames, ASDUs, and information objects. This is
intentional because protocol integrations often need raw bytes for diagnostics
and for vendor-specific edge cases.

`Iec104StreamDecoder` is stateful because it buffers incomplete APDUs. It is a
parser object, not a session manager. Use one decoder per TCP stream/session,
do not share it across concurrent sessions, and call `reset()` only when
buffered bytes should be discarded, such as after a reconnect.

`Iec104StreamDecoder` is permissive by default: malformed recognized ASDUs may
return the information objects that can still be parsed. Construct it with
`new Iec104StreamDecoder(true)` to enable strict ASDU parsing, where truncated
recognized typed ASDUs are returned as `ParseResult.error()` entries. Strict
mode does not reject recognized raw-only catalog entries or unknown Type IDs;
those continue to expose raw bytes for diagnostics.

The SDK stops at parsing. IEC104 STARTDT/STOPDT lifecycle decisions, TESTFR
heartbeat policy, reconnects, command routing, select-before-operate workflow,
device registry, storage, batching, and backpressure belong in a runtime layer
that consumes this parser.

## Compatibility

- Source compatibility target: Java 8.
- Release builds can run on JDK 21 with `--release 8`.
- CI verifies the project on JDK 8 and JDK 21.
- The current stable Maven Central release is `0.6.0`; `0.5.0` remains the
  previous IEC101/IEC103 completion release.

## Build

```bash
mvn -q verify
```

Java 8 compatibility check used by local development:

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-8.jdk/Contents/Home \
PATH=/Library/Java/JavaVirtualMachines/zulu-8.jdk/Contents/Home/bin:$PATH \
mvn -q verify
```

Release artifacts include source jars, Javadoc jars, checksums, and GPG
signatures. The release process is documented in
[`docs/release.md`](docs/release.md).

## Runtime Platform

Future collector runtime work is planned as a separate JDK 21 layer that
consumes these Java 8 compatible SDK modules without adding runtime
dependencies back into the SDK. The architecture boundary is documented in
[`docs/runtime-platform-architecture.md`](docs/runtime-platform-architecture.md),
and the ingestion adapter roadmap is documented in
[`docs/runtime-ingestion-roadmap.md`](docs/runtime-ingestion-roadmap.md).

## Design Constraints

- Keep parser modules independent from collector runtimes.
- Do not introduce Spring, Netty, database, Redis, MQ, or runtime global
  dependencies into protocol modules.
- Prefer strongly typed protocol models over `Map<String, Object>` style
  parser output.
- Preserve raw bytes wherever useful for diagnostics and unsupported edge cases.

## Roadmap

- Execute the `0.7.0` IEC104 hardening plan before adding a new
  runtime-oriented protocol surface. The plan is tracked in
  [`docs/release-plan-0.7.0.md`](docs/release-plan-0.7.0.md).
- The `0.6.0` readiness decision is tracked in
  [`docs/release-readiness-0.6.0.md`](docs/release-readiness-0.6.0.md), with
  release notes in [`docs/release-notes-0.6.0.md`](docs/release-notes-0.6.0.md).
- Future SDK phases can add more protocol helpers or broader Modbus function
  coverage without moving runtime concerns into this parser SDK.
- Build a future collector runtime on JDK 21 outside this SDK repository. The
  runtime architecture and ingestion roadmap are tracked in
  [`docs/runtime-platform-architecture.md`](docs/runtime-platform-architecture.md)
  and [`docs/runtime-ingestion-roadmap.md`](docs/runtime-ingestion-roadmap.md).
