# Protocol SDK

Protocol SDK is a standalone Java protocol parsing SDK. It is split from a
legacy collector runtime and intentionally keeps only protocol parsing,
streaming decoders, and strongly typed protocol models.

The first public baseline started with IEC 60870-5-104, and the current
published SDK line also includes IEC101, IEC103, and experimental Modbus
modules. The SDK does not depend on Spring, Netty, databases, Redis, message
queues, or collector runtime globals.

## Maven Central

Current stable release: `0.5.0`

Use the protocol module directly in applications:

```xml
<dependency>
    <groupId>io.github.qbsstg</groupId>
    <artifactId>protocol-iec104</artifactId>
    <version>0.5.0</version>
</dependency>
```

The shared core contracts are published separately and are pulled transitively
by `protocol-iec104`:

```xml
<dependency>
    <groupId>io.github.qbsstg</groupId>
    <artifactId>protocol-core</artifactId>
    <version>0.5.0</version>
</dependency>
```

The parent `protocol-sdk` POM is for building this repository. Most users should
depend on protocol modules such as `protocol-iec104`.

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
| `protocol-modbus` | Experimental, `0.6.0` target | Modbus TCP/UDP ADU, PDU, typed value, and exception parser. [Design note](docs/protocol-modbus-design.md), [0.6.0 plan](docs/release-plan-0.6.0.md). |
| `protocol-http` | Planned | HTTP protocol helpers for collection scenarios. |

`0.6.0` is planned as the Modbus stable-completion release. Modbus remains
experimental until the `0.6.0` gates in
[`docs/release-plan-0.6.0.md`](docs/release-plan-0.6.0.md) are complete.

## IEC104 Coverage

`protocol-iec104` currently recognizes 45 ASDU types and returns typed values
for every recognized type. Unknown type IDs are preserved as raw bytes for
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

`Iec104StreamDecoder` is stateful because it buffers incomplete APDUs. Use one
decoder per TCP stream/session, and call `reset()` only when buffered bytes
should be discarded, such as after a reconnect.

`Iec104StreamDecoder` is permissive by default: malformed recognized ASDUs may
return the information objects that can still be parsed. Use
`new Iec104StreamDecoder(true)` to enable strict ASDU parsing, where truncated
recognized information objects are returned as `ParseResult.error()` entries.

## Compatibility

- Source compatibility target: Java 8.
- Release builds can run on JDK 21 with `--release 8`.
- CI verifies the project on JDK 8 and JDK 21.
- The current stable Maven Central release is `0.5.0`.

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

- Complete an IEC104 conformance and gap audit before adding more IEC104 types.
- Focus the next SDK release phase on Modbus TCP/UDP stable completion. The SDK
  release plan is tracked in [`docs/sdk-release-roadmap.md`](docs/sdk-release-roadmap.md)
  and [`docs/release-plan-0.6.0.md`](docs/release-plan-0.6.0.md).
- The `0.5.0` readiness decision is tracked in
  [`docs/release-readiness-0.5.0.md`](docs/release-readiness-0.5.0.md).
- Keep Modbus experimental until the `0.6.0` stability gates are complete.
- Build a future collector runtime on JDK 21 outside this SDK repository. The
  runtime architecture and ingestion roadmap are tracked in
  [`docs/runtime-platform-architecture.md`](docs/runtime-platform-architecture.md)
  and [`docs/runtime-ingestion-roadmap.md`](docs/runtime-ingestion-roadmap.md).
