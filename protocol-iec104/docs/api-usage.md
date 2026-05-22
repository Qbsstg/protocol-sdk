# IEC104 API Usage Guide

This guide describes the public `protocol-iec104` API for applications that
consume the SDK from Maven Central. The examples use Java 8-compatible syntax.

## Dependency

Most applications should depend on `protocol-iec104` directly:

```xml
<dependency>
    <groupId>io.github.qbsstg</groupId>
    <artifactId>protocol-iec104</artifactId>
    <version>0.1.0</version>
</dependency>
```

The shared `protocol-core` contracts are pulled transitively.

## Decoder Lifecycle

`Iec104StreamDecoder` is a stateful streaming decoder. It buffers incomplete
APDUs internally, so callers can feed bytes as they arrive from a TCP session.

Use one decoder per IEC104 stream/session:

```java
Iec104StreamDecoder decoder = new Iec104StreamDecoder();
List<ParseResult<Iec104Frame>> results = decoder.decode(tcpPayload);
```

Do not share one decoder across unrelated devices, TCP sessions, or concurrent
threads. Create a new decoder for each session, or call `reset()` when a session
is replaced and any buffered partial bytes should be discarded.

## Handling Parse Results

`decode(byte[])` returns zero or more `ParseResult<Iec104Frame>` entries. An
empty result usually means the decoder is waiting for the rest of an incomplete
APDU.

```java
List<ParseResult<Iec104Frame>> results = decoder.decode(tcpPayload);
for (ParseResult<Iec104Frame> result : results) {
    if (result.isSuccess()) {
        handleFrame(result.getFrame());
    } else if (result.isError()) {
        System.err.println("IEC104 parse error: " + result.getMessage());
    }
}
```

The decoder can recover from noise and invalid APDU lengths by returning an
error result and consuming the invalid bytes reported by `getConsumedBytes()`.

## Inspecting Frames and ASDUs

Successful I-format frames contain an `Iec104Asdu`:

```java
Iec104Frame frame = result.getFrame();
if (frame.getAsdu() == null) {
    return; // S-format or U-format frame
}

Iec104Asdu asdu = frame.getAsdu();
Iec104AsduType type = asdu.getType();
int commonAddress = asdu.getCommonAddress();
```

Each ASDU contains information objects with an IEC104 three-octet information
object address, raw element bytes, and a typed value when the Type ID is
supported:

```java
for (Iec104InformationObject object : asdu.getInformationObjects()) {
    int informationObjectAddress = object.getAddress();
    byte[] elementBytes = object.getElementBytes();
    Iec104InformationValue value = object.getValue();
}
```

Typed values are grouped by model class. For example:

```java
if (object.getValue() instanceof Iec104SinglePointValue) {
    Iec104SinglePointValue value = (Iec104SinglePointValue) object.getValue();
    boolean on = value.isOn();
    Iec104Cp56Time2a timeTag = value.getTimeTag();
}
```

## Checking ASDU Support

Use `Iec104AsduSupport` before casting when the Type ID is not known at compile
time:

```java
Iec104AsduSupport support = Iec104AsduSupport.ofTypeId(asdu.getTypeId());
if (support.hasTypedValue()) {
    Class<? extends Iec104InformationValue> valueClass = support.getValueClass();
} else if (support.isRawBytesOnly()) {
    byte[] rawAsdu = asdu.getRawBytes();
} else if (support.isUnknownType()) {
    byte[] rawAsdu = asdu.getRawBytes();
}
```

The human-readable support matrix is maintained in
[`asdu-support-matrix.md`](asdu-support-matrix.md).

## Raw-Byte Fallback

Raw bytes remain available at every level:

- `Iec104Frame.getRawBytes()` returns the complete APDU.
- `Iec104Asdu.getRawBytes()` returns the ASDU bytes after APCI.
- `Iec104InformationObject.getElementBytes()` returns the information element
  bytes for one object.
- `Iec104Cp56Time2a.getRawBytes()` returns the original seven timestamp bytes.

For unknown Type IDs, `Iec104Asdu.getType()` returns `Iec104AsduType.UNKNOWN`,
`Iec104InformationObject.getValue()` returns `null`, and raw bytes are still
preserved for diagnostics or vendor-specific handling.

## Java Compatibility

The SDK source and public examples stay Java 8 compatible. Repository builds can
run on JDK 21 while compiling release artifacts with `--release 8`. CI verifies
the project on JDK 8 and JDK 21.

Use Maven Central version `0.1.0` for stable consumption. The repository
`main` branch currently uses `0.1.1-SNAPSHOT` for ongoing development.
