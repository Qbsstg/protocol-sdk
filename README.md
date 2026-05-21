# Protocol SDK

This repository contains a standalone protocol parsing SDK. It is intentionally
separate from any collector runtime. The SDK only owns protocol parsing and
strongly typed protocol models.

## Modules

- `protocol-core`: shared Java 8 compatible parser contracts and result types.
- `protocol-iec104`: IEC60870-5-104 frame, ASDU, information object, and typed value parser.

Planned modules:

- `protocol-iec101`
- `protocol-iec103`
- `protocol-modbus`
- `protocol-http`

## Dependency

After the SDK is published, applications should depend on protocol modules
directly instead of the parent POM:

```xml
<dependency>
    <groupId>io.github.qbsstg</groupId>
    <artifactId>protocol-iec104</artifactId>
    <version>0.1.0</version>
</dependency>
```

The parent `protocol-sdk` POM is only for building the SDK workspace.

## Quick Usage

```java
import io.github.qbsstg.protocol.core.ParseResult;
import io.github.qbsstg.protocol.iec104.Iec104Frame;
import io.github.qbsstg.protocol.iec104.Iec104InformationObject;
import io.github.qbsstg.protocol.iec104.Iec104SinglePointValue;
import io.github.qbsstg.protocol.iec104.Iec104StreamDecoder;

import java.util.List;

Iec104StreamDecoder decoder = new Iec104StreamDecoder();
List<ParseResult<Iec104Frame>> results = decoder.decode(new byte[] {
        0x68, 0x0E,
        0x00, 0x00, 0x00, 0x00,
        0x01, 0x01, 0x03, 0x00,
        0x01, 0x00, 0x01, 0x00, 0x00, 0x01
});

ParseResult<Iec104Frame> result = results.get(0);
if (result.isSuccess()) {
    Iec104Frame frame = result.getFrame();
    Iec104InformationObject object = frame.getAsdu().getInformationObjects().get(0);
    Iec104SinglePointValue value = (Iec104SinglePointValue) object.getValue();
    boolean on = value.isOn();
}
```

The decoder is stream-oriented. It buffers incomplete APDUs internally, so
callers can feed bytes directly from TCP packets without pre-splitting frames.

## Public API Shape

The current API is layered:

- `ByteStreamDecoder<T>`: streaming parser contract.
- `ParseResult<T>`: success, incomplete, or error result.
- `Iec104Frame`: APDU frame type and send/receive sequence numbers.
- `Iec104Asdu`: ASDU header, cause of transmission, common address, and objects.
- `Iec104InformationObject`: information object address, raw bytes, typed value.
- `Iec104InformationValue`: marker interface for typed IEC104 values.
- `Iec104AsduSupport`: typed/raw-only/unknown support status for ASDU types.

Typed values currently include:

- `Iec104SinglePointValue`
- `Iec104DoublePointValue`
- `Iec104StepPositionValue`
- `Iec104BitstringValue`
- `Iec104MeasuredValue`
- `Iec104IntegratedTotalsValue`
- `Iec104PackedSinglePointValue`
- `Iec104SingleProtectionEventValue`
- `Iec104PackedStartEventsValue`
- `Iec104PackedOutputCircuitValue`
- `Iec104ProtectionQualityDescriptor`
- `Iec104ParameterMeasuredValue`
- `Iec104ParameterQualifier`
- `Iec104ParameterActivationValue`
- `Iec104Cp56Time2a`
- `Iec104QualityDescriptor`
- `Iec104SingleCommandValue`
- `Iec104DoubleCommandValue`
- `Iec104RegulatingStepCommandValue`
- `Iec104SetPointCommandValue`
- `Iec104BitstringCommandValue`
- `Iec104InterrogationCommandValue`
- `Iec104CounterInterrogationCommandValue`
- `Iec104ReadCommandValue`
- `Iec104ClockSynchronizationCommandValue`
- `Iec104ResetProcessCommandValue`
- `Iec104DelayAcquisitionCommandValue`

Raw bytes remain available on frames, ASDUs, and information objects. This is
intentional: protocol integrations often need raw bytes for diagnostics and for
handling vendor-specific edge cases.

## Design Constraints

- No Spring, Netty, MySQL, Redis, RabbitMQ, or runtime global state.
- Public APIs should be strongly typed. Do not expose the legacy
  `Map<String, Object>` parser contract.
- Source remains Java 8 compatible.
- Release builds may use JDK 21 with `--release 8`.
- Parser modules must not depend on collector runtime packages.

## Maven Central Package Plan

The intended Maven coordinates are:

| Module | Artifact |
| --- | --- |
| Parent build | `io.github.qbsstg:protocol-sdk` |
| Core contracts | `io.github.qbsstg:protocol-core` |
| IEC104 parser | `io.github.qbsstg:protocol-iec104` |
| Future IEC101 parser | `io.github.qbsstg:protocol-iec101` |
| Future IEC103 parser | `io.github.qbsstg:protocol-iec103` |
| Future Modbus parser | `io.github.qbsstg:protocol-modbus` |
| Future HTTP protocol helpers | `io.github.qbsstg:protocol-http` |

Before public release, the repository still needs:

- Maven Central publishing configuration and signing.
- More conformance tests with real-world frames.

The build already attaches source and Javadoc jars during `verify`, so release
artifacts can be checked locally before publishing credentials are configured:

```bash
mvn -q verify
```

## Local Verification

```bash
mvn -q test
```

Java 8 compatibility check:

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-8.jdk/Contents/Home \
PATH=/Library/Java/JavaVirtualMachines/zulu-8.jdk/Contents/Home/bin:$PATH \
mvn -q test
```
