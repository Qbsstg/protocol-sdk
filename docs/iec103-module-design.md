# IEC103 Module Design

This note defines the intended boundaries and first public API shape for the
future `protocol-iec103` module. The first goal is a Java 8 compatible parser
for IEC 60870-5-103 protection relay communication scenarios, independent of
collector runtime code and transport frameworks.

## Goals

- Add IEC 60870-5-103 parsing as a standalone SDK module.
- Support protection relay link-frame parsing separately from ASDU parsing.
- Model protection events, measurands, identification data, and raw fallback
  values in IEC103-named public classes.
- Preserve raw bytes at frame, ASDU, and information-element levels.
- Keep the module runtime-independent and suitable for Maven Central use.
- Keep the design compatible with the existing IEC101 and IEC104 parser
  contracts without exposing IEC104 public classes from IEC103.

## Non-goals

- Do not implement a protection relay master station, polling scheduler, retry
  policy, or event database in this parser module.
- Do not add serial-port, TCP, Netty, MQTT, Kafka, Spring, database, Redis, or
  collector-runtime dependencies.
- Do not claim full IEC103 conformance in the first implementation.
- Do not expose IEC104-named public value classes from the IEC103 API.
- Do not add disturbance-record transfer as typed values until real fixtures
  are available.

## Module Boundary

`protocol-iec103` should be a Maven module beside the current protocol modules:

```text
protocol-sdk
|-- protocol-core
|-- protocol-iec104
|-- protocol-iec101
|-- protocol-modbus
`-- protocol-iec103
```

Initial dependencies:

- `protocol-core`: required for `ByteStreamDecoder`, `ProtocolFrame`, and
  `ParseResult`.
- JUnit only in tests.
- No dependency on `protocol-iec104` in the public API.
- No dependency on `protocol-iec101`, unless a later internal package extracts
  a neutral FT1.2 link-frame helper without leaking IEC101 names.

IEC103 uses the FT1.2 link-frame family also used by IEC101, but IEC103 ASDU
payloads are protection-relay specific. The first implementation can reuse the
IEC101 stream decoder as an implementation reference while keeping public types
named and shaped for IEC103.

## Public API Shape

Package:

```java
io.github.qbsstg.protocol.iec103
```

Primary decoder:

```java
Iec103StreamDecoder decoder = new Iec103StreamDecoder(Iec103ParserConfig.defaultUnbalanced());
List<ParseResult<Iec103Frame>> results = decoder.decode(bytes);
```

Proposed public classes:

| Type | Responsibility |
| --- | --- |
| `Iec103StreamDecoder` | Stateful byte-stream parser for IEC103 FT1.2 link frames. One decoder per link session. |
| `Iec103ParserConfig` | Link address, common address, strictness, and maximum frame-size configuration. |
| `Iec103Frame` | Implements `ProtocolFrame`; exposes raw bytes, frame format, link control, link address, and optional ASDU. |
| `Iec103FrameFormat` | `SINGLE_CHARACTER`, `FIXED_LENGTH`, `VARIABLE_LENGTH`. |
| `Iec103LinkControl` | Decoded control field bits and function code. |
| `Iec103LinkFunction` | IEC103 link-function interpretation. |
| `Iec103Asdu` | IEC103 ASDU header, cause, common address, raw bytes, and information elements. |
| `Iec103AsduType` | Known IEC103 type identifiers with raw integer fallback. |
| `Iec103VariableStructureQualifier` | Number of information elements and sequence flag. |
| `Iec103InformationElement` | Function type, information number, raw element bytes, and optional typed value. |
| `Iec103InformationValue` | Marker interface for typed IEC103 values. |
| `Iec103CauseOfTransmission` | Known IEC103 COT values with raw integer fallback. |
| `Iec103ProtectionEventValue` | Protection event state, quality, optional relative time, fault number, and optional time tag. |
| `Iec103MeasuredValue` | Measurand kind, raw value, engineering value, and quality/status flags. |
| `Iec103IdentificationValue` | Relay or compatible-function identification payload, raw-backed where fields are vendor-specific. |
| `Iec103TimeTag` | IEC103-named time model for the first time-tag format implemented by the module. |
| `Iec103Support` | Typed/raw-only/unknown support status for ASDU Type IDs. |

The first config should be explicit rather than hiding deployment choices:

```java
Iec103ParserConfig config = Iec103ParserConfig.builder()
        .linkAddressLength(1)
        .commonAddressLength(1)
        .maxFrameLength(255)
        .strictChecksum(true)
        .preserveUnknownTypePayload(true)
        .build();
```

The first release should support one-octet link and common addresses by
default. Additional address-size variants should be added only with fixtures.

## Link-layer Model

IEC103 should initially support the same FT1.2 frame shapes needed by common
protection relay links:

- Single-character acknowledgement frame such as `0xE5`.
- Fixed-length frames for link commands and confirmations.
- Variable-length frames carrying ASDUs.
- Control field parsing.
- Link address parsing.
- Checksum validation.
- Split-frame buffering.
- Concatenated-frame decoding.
- Noise and malformed-frame recovery.

The decoder should parse bytes and expose diagnostics. It should not own relay
session policy such as general interrogation cycles, timeout handling, retry
windows, FCB/FCV tracking, or disturbance transfer orchestration.

## ASDU Parsing Boundary

IEC103 parsing should remain layered:

```text
raw bytes -> Iec103Frame -> Iec103Asdu -> Iec103InformationElement -> typed value
```

The ASDU decoder should parse these protocol-level fields first:

- Type identification.
- Variable structure qualifier.
- Cause of transmission.
- Common address.
- Function type (`FUN`).
- Information number (`INF`).
- Raw information bytes.

Unlike IEC101 and IEC104, IEC103 protection payloads are commonly addressed by
`FUN` and `INF` rather than a generic information-object address. The API
should therefore expose `functionType` and `informationNumber` as first-class
fields on `Iec103InformationElement`.

Unsupported and unknown ASDU types must remain parseable as raw bytes when the
frame and ASDU envelope are structurally valid.

## First Supported Scope

The first `protocol-iec103` implementation should be narrow, fixture-driven,
and useful for protection relay integrations.

Frame support:

- Single-character acknowledgement frame.
- Fixed-length link frame with one-octet link address.
- Variable-length frame with one-octet link address and ASDU payload.
- Checksum validation.
- Split payload buffering.
- Concatenated frame decoding.
- Noise and invalid-length recovery.

Typed ASDU support:

| Type ID | Practical name | First support |
| --- | --- | --- |
| `1` | Time-tagged message | Typed `Iec103ProtectionEventValue`. |
| `2` | Time-tagged message with relative time | Typed `Iec103ProtectionEventValue` with relative time and fault number when present. |
| `3` | Measurands I | Typed `Iec103MeasuredValue`. |
| `4` | Time-tagged measurands with relative time | Typed measured value plus relative time metadata when fixture-backed. |
| `5` | Identification | Typed `Iec103IdentificationValue` for stable fields, raw-backed for vendor data. |
| `9` | Measurands II | Typed `Iec103MeasuredValue`. |

Raw-only first set:

- Time synchronization and general interrogation command/response frames until
  command modeling is backed by tests.
- Generic data and generic identification payloads.
- Disturbance-record directory and disturbance-transfer ASDUs.
- Vendor-specific or private Type IDs.

This split keeps the first module useful for event and measurand decoding while
avoiding unsupported claims around disturbance-file workflows.

## Protection Event Modeling

IEC103 should use IEC103-named public values but keep concepts consistent with
the IEC104 protection-event model where that helps callers.

`Iec103ProtectionEventValue` should expose:

- `asduType`.
- `functionType`.
- `informationNumber`.
- `rawEvent`.
- `eventState`, using an IEC103-named enum.
- `quality`, using an IEC103-named quality descriptor.
- `elapsedTimeMillis` or `relativeTimeMillis` when the ASDU contains relative
  time.
- `faultNumber` when the ASDU contains fault identification.
- `timeTag` when a supported IEC103 time tag is present.
- `rawBytes`.

The quality descriptor should preserve the raw value and expose common flags
such as invalid, not topical, substituted, blocked, and elapsed-time invalid
when those bits are present in the encoded event. The exact bit mapping should
be verified by fixtures before typed support is marked complete.

The IEC104 classes `Iec104SingleProtectionEventValue`,
`Iec104SingleProtectionEventState`, and `Iec104ProtectionQualityDescriptor`
should remain implementation references only. Returning those classes from
IEC103 would leak protocol names and make the public API harder to evolve.

## Measured Value Modeling

`Iec103MeasuredValue` should expose:

- `asduType`.
- `functionType`.
- `informationNumber`.
- `kind`, such as `MEASURANDS_I`, `MEASURANDS_II`, or `TIME_TAGGED`.
- `rawValue`.
- `value`.
- `quality`.
- Optional relative-time and time-tag metadata.
- `rawBytes`.

The parser should preserve unsigned wire fields as Java `int`, copy raw byte
arrays defensively, and keep value conversion rules visible in tests.

## Test Strategy

Executable tests should be fixture-driven and should not require serial ports,
relays, files, sockets, or external services.

Frame tests:

- Single-character acknowledgement.
- Fixed-length frame with one-octet link address.
- Variable-length frame with ASDU payload.
- Bad checksum.
- Invalid repeated length.
- Split payload buffering.
- Concatenated frames.
- Noise before a valid frame.
- Maximum frame-size rejection.

ASDU tests:

- ASDU header field extraction.
- `FUN` and `INF` parsing.
- Unknown Type ID raw-byte preservation.
- Raw-only supported Type ID reporting.
- Time-tagged protection event fixture.
- Time-tagged protection event with relative time fixture.
- Measurands I fixture.
- Measurands II fixture.
- Identification fixture.
- Malformed information-element length handling.

Build verification:

```bash
mvn -q verify
```

The module must remain Java 8 source compatible and pass the existing JDK 8 and
JDK 21 CI matrix.

## Implementation Order

1. Add `protocol-iec103` module skeleton and Maven reactor wiring.
2. Add parser config, frame, link-control, ASDU, information-element, and
   support-status models.
3. Implement FT1.2 frame decoding for single-character, fixed-length, and
   variable-length frames.
4. Add checksum, buffering, concatenation, malformed-frame, and recovery tests.
5. Add raw ASDU header parsing and `FUN`/`INF` extraction.
6. Add typed values for time-tagged protection events and measurands.
7. Add identification and raw-only support matrix documentation.
8. Add fixture-backed disturbance/generic data support in later slices.

## Acceptance Mapping

| Issue #15 criterion | Design answer |
| --- | --- |
| First practical IEC103 frame types and information elements | FT1.2 single-character, fixed-length, and variable-length frames; typed protection event, measurand, and identification first set; raw-only generic and disturbance data. |
| Module boundaries and model classes | `protocol-iec103` depends on `protocol-core`, exposes IEC103-named frame, ASDU, element, value, and support classes, and avoids runtime dependencies. |
| Protection event consistency with IEC104 | IEC103 values mirror useful event-state, quality, relative-time, and time-tag concepts but do not expose IEC104 public classes. |
| Java 8/runtime independence | The design requires Java 8 compatibility, defensive raw-byte copies, no transport framework, and no collector runtime state. |
| Test fixtures | Fixture categories cover link frames, ASDU envelope parsing, `FUN`/`INF`, typed events, measured values, raw-only values, and malformed inputs. |
