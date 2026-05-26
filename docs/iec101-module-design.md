# IEC101 Module Design

This note defines the intended boundaries and public API shape for the future
`protocol-iec101` module. It is a design checkpoint before implementation and
keeps the module Java 8 compatible with no runtime dependencies.

## Goals

- Add IEC 60870-5-101 parsing as a standalone SDK module.
- Support serial/link-layer frame parsing separately from ASDU parsing.
- Preserve raw bytes at frame, ASDU, and information-object levels.
- Keep parser output strongly typed where the ASDU model is implemented.
- Avoid Spring, Netty, database, Redis, MQ, scheduler, or collector-runtime
  dependencies.

## Non-goals

- Do not implement a full master/slave polling scheduler in the parser module.
- Do not add serial-port, TCP, Netty, MQTT, or Kafka transport code.
- Do not expose IEC104-named public model classes from the IEC101 API.
- Do not claim complete IEC101 conformance in the first implementation.

## Module Boundary

`protocol-iec101` should be a Maven module beside `protocol-iec104`:

```text
protocol-sdk
|-- protocol-core
|-- protocol-iec104
`-- protocol-iec101
```

Initial dependencies:

- `protocol-core`: required for `ByteStreamDecoder`, `ProtocolFrame`, and
  `ParseResult`.
- JUnit only in tests.
- No dependency on `protocol-iec104` in the public API.

IEC101 and IEC104 share many application-layer ASDU concepts, but the current
IEC104 classes are named and shaped for IEC104 defaults. Directly returning
`Iec104Asdu`, `Iec104InformationObject`, or `Iec104...Value` from an IEC101
module would leak the wrong protocol name and make future API cleanup harder.

Recommended reuse strategy:

- Reuse existing IEC104 parser tests and byte fixtures as implementation
  references, not as public types.
- Start `protocol-iec101` with IEC101-named public models.
- Extract a neutral common IEC 60870 model later only if duplication becomes
  meaningful. That extraction should use package-neutral names such as
  `Iec60870AsduType` and should not be required for the first IEC101 baseline.

## Public API Shape

Package:

```java
io.github.qbsstg.protocol.iec101
```

Primary decoder:

```java
Iec101StreamDecoder decoder = new Iec101StreamDecoder(Iec101ParserConfig.defaultUnbalanced());
List<ParseResult<Iec101Frame>> results = decoder.decode(bytes);
```

Proposed public classes:

| Type | Responsibility |
| --- | --- |
| `Iec101StreamDecoder` | Stateful byte-stream parser for IEC101 link frames. One decoder per serial/link session. |
| `Iec101ParserConfig` | Transmission mode and address-size configuration. |
| `Iec101TransmissionMode` | `BALANCED` or `UNBALANCED`. |
| `Iec101Frame` | Implements `ProtocolFrame`; exposes raw bytes, frame format, link control, link address, and optional ASDU. |
| `Iec101FrameFormat` | `SINGLE_CHARACTER`, `FIXED_LENGTH`, `VARIABLE_LENGTH`. |
| `Iec101LinkControl` | Decoded control field bits and function code. |
| `Iec101LinkFunction` | Mode-aware function-code interpretation. |
| `Iec101Asdu` | IEC101 ASDU header, cause, common address, raw bytes, and information objects. |
| `Iec101InformationObject` | Information object address, raw element bytes, and optional typed value. |
| `Iec101InformationValue` | Marker interface for typed IEC101 values. |
| `Iec101AsduSupport` | Typed/raw-only/unknown support status for ASDU Type IDs. |

Address configuration should be explicit because IEC101 deployments vary:

```java
Iec101ParserConfig config = Iec101ParserConfig.builder()
        .transmissionMode(Iec101TransmissionMode.UNBALANCED)
        .linkAddressLength(1)
        .causeOfTransmissionLength(2)
        .commonAddressLength(2)
        .informationObjectAddressLength(3)
        .build();
```

The first implementation should support link address lengths of one or two
octets. Zero-length link addresses can be deferred until there is a real trace
or integration need.

## Link-layer Model

IEC101 requires link-layer models that IEC104 does not have:

- Single-character acknowledgement frames such as `0xE5`.
- Fixed-length frames for link commands and confirmations.
- Variable-length frames carrying ASDUs.
- Link control-field bits and function-code interpretation.
- Link address bytes and checksum validation.
- Balanced and unbalanced transmission-mode semantics.

The parser should decode these fields and preserve raw bytes. It should not own
the higher-level polling/session policy. A future runtime can build a session
manager on top of parsed frames if it needs timeout handling, retry handling,
FCB/FCV tracking, or command scheduling.

## Balanced and Unbalanced Contracts

The same byte decoder can parse both modes, but control-field interpretation
must be mode-aware.

Unbalanced mode:

- The module should expose whether a frame is primary or secondary.
- Function-code names should match the unbalanced direction semantics.
- The parser should not enforce polling order or retry policy.

Balanced mode:

- The module should expose the balanced function-code interpretation.
- Both peers may initiate traffic; the parser should not assume a master-only
  direction.
- Link-session state remains outside the first parser baseline.

Both modes:

- Bad checksum and invalid length return `ParseResult.error(...)`.
- Incomplete frames remain buffered until more bytes arrive.
- Noise before a valid start byte is skipped or returned as an error using the
  same recovery style as `protocol-iec104`.

## ASDU Parsing Boundary

IEC101 ASDU parsing should be separate from link-frame parsing:

```text
raw bytes -> Iec101Frame -> Iec101Asdu -> Iec101InformationObject -> typed value
```

The ASDU decoder must use `Iec101ParserConfig` for:

- Cause-of-transmission length.
- Common-address length.
- Information-object-address length.

It should preserve unknown and unsupported Type IDs as raw bytes. Typed values
can be added incrementally and tracked by `Iec101AsduSupport`.

## First Supported Scope

The first `protocol-iec101` implementation should keep scope narrow enough to
be useful and reviewable.

Frame support:

- Single-character acknowledgement frame.
- Fixed-length link frame with one- or two-octet link address.
- Variable-length frame with one- or two-octet link address and ASDU payload.
- Checksum validation.
- Split payload buffering.
- Concatenated frame decoding.
- Noise and invalid-length recovery.

ASDU support:

- Parse ASDU headers for configurable COT/common-address lengths.
- Parse information object addresses for configurable one-, two-, or
  three-octet lengths.
- Preserve raw ASDU and information-element bytes for every frame.
- Typed first set:
  - `M_SP_NA_1` single-point information.
  - `M_DP_NA_1` double-point information.
  - `M_ME_NA_1` normalized measured value.
  - `M_ME_NB_1` scaled measured value.
  - `M_ME_NC_1` short floating point measured value.
  - `C_SC_NA_1` single command.
  - `C_DC_NA_1` double command.
  - `C_IC_NA_1` interrogation command.

Follow-up time-tagged support adds IEC101-named timestamp models:

- `Iec101Cp24Time2a` for selected CP24 process-value variants.
- `Iec101Cp56Time2a` for selected CP56 process-value variants and
  `C_CS_NA_1` clock synchronization.

Step position, bitstring, integrated totals, protection events, and
time-tagged command variants remain deferred until their base public models are
added or real integration demand justifies the API.

## Test Strategy

Executable tests should be fixture-driven and should not require serial ports
or external services.

Frame tests:

- Single-character acknowledgement.
- Fixed-length frame with one-octet and two-octet link addresses.
- Variable-length frame with ASDU payload.
- Bad checksum.
- Invalid repeated length.
- Split payload buffering.
- Concatenated frames.
- Noise before valid frame.

ASDU tests:

- COT length one and two.
- Common-address length one and two.
- Information-object-address length one, two, and three.
- Single object and SQ sequence addressing.
- Unknown Type ID raw-byte preservation.
- Typed first-set value decoding.

Mode tests:

- Balanced and unbalanced control-field interpretation.
- Primary/secondary direction bits.
- Function-code mapping per mode.

Build verification:

```bash
mvn -q verify
```

The module must remain Java 8 source compatible and pass the existing JDK 8 and
JDK 21 CI matrix.

## Implementation Order

1. Add `protocol-iec101` module skeleton and Maven wiring.
2. Add configuration and link-frame model classes.
3. Implement frame-level decoder for single-character, fixed-length, and
   variable-length frames.
4. Add checksum, buffering, concatenation, and recovery tests.
5. Add raw ASDU header and information-object parsing.
6. Add typed first-set ASDU values and `Iec101AsduSupport`.
7. Add README and support-matrix documentation for the module.

## Acceptance Mapping

| Issue #12 criterion | Design answer |
| --- | --- |
| Module boundaries | `protocol-iec101` depends on `protocol-core`; IEC101 public API does not expose IEC104-named models. |
| Public API shape | Decoder, config, link-frame, ASDU, information-object, value, and support classes are listed above. |
| Balanced/unbalanced contracts | Mode-aware control-field interpretation is in the parser; session policy stays outside the SDK module. |
| First supported frame and ASDU set | Initial frame support and typed ASDU first set are listed in the first supported scope. |
| Java 8/no runtime dependencies | The design requires Java 8 syntax and no transport/runtime framework dependencies. |
