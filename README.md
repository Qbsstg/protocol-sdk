# Protocol SDK

Protocol SDK is a standalone Java protocol parsing SDK. It is split from a
legacy collector runtime and intentionally keeps only protocol parsing,
streaming decoders, and strongly typed protocol models.

The first published module is IEC 60870-5-104. The SDK does not depend on
Spring, Netty, databases, Redis, message queues, or collector runtime globals.

## Maven Central

Latest stable release: `0.1.0`

Use the protocol module directly in applications:

```xml
<dependency>
    <groupId>io.github.qbsstg</groupId>
    <artifactId>protocol-iec104</artifactId>
    <version>0.1.0</version>
</dependency>
```

The shared core contracts are published separately and are pulled transitively
by `protocol-iec104`:

```xml
<dependency>
    <groupId>io.github.qbsstg</groupId>
    <artifactId>protocol-core</artifactId>
    <version>0.1.0</version>
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
[`Iec104SdkUsageExampleTest`](protocol-iec104/src/test/java/io/github/qbsstg/protocol/iec104/Iec104SdkUsageExampleTest.java).

## Modules

| Module | Status | Purpose |
| --- | --- | --- |
| `protocol-core` | Published | Shared Java 8 compatible parser contracts and result types. |
| `protocol-iec104` | Published | IEC 60870-5-104 APDU, ASDU, information object, and typed value parser. |
| `protocol-iec101` | Planned | IEC 60870-5-101 parser. |
| `protocol-iec103` | Planned | IEC 60870-5-103 parser. |
| `protocol-modbus` | Planned | Modbus TCP/UDP parser. |
| `protocol-http` | Planned | HTTP protocol helpers for collection scenarios. |

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

`Iec104StreamDecoder` is permissive by default: malformed recognized ASDUs may
return the information objects that can still be parsed. Use
`new Iec104StreamDecoder(true)` to enable strict ASDU parsing, where truncated
recognized information objects are returned as `ParseResult.error()` entries.

## Compatibility

- Source compatibility target: Java 8.
- Release builds can run on JDK 21 with `--release 8`.
- CI verifies the project on JDK 8 and JDK 21.
- The current `main` branch uses the next development version
  `0.1.1-SNAPSHOT`; use `0.1.0` for stable Maven Central consumption.

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

## Design Constraints

- Keep parser modules independent from collector runtimes.
- Do not introduce Spring, Netty, database, Redis, MQ, or runtime global
  dependencies into protocol modules.
- Prefer strongly typed protocol models over `Map<String, Object>` style
  parser output.
- Preserve raw bytes wherever useful for diagnostics and unsupported edge cases.

## Roadmap

- Complete an IEC104 conformance and gap audit before adding more IEC104 types.
- Add IEC101, IEC103, and Modbus modules as separate SDK modules.
- Build a future collector runtime on JDK 21 outside this SDK repository.
