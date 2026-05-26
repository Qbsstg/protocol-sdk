# IEC103 ASDU Support Matrix

This document is the human-readable support matrix and completion audit for
`protocol-iec103`. The executable counterparts are `Iec103SupportMatrixTest`
and the parser fixtures under `protocol-iec103/src/test/java`.

## Support Levels

| Level | Meaning |
| --- | --- |
| Typed value | `Iec103InformationElement.getValue()` returns a typed `Iec103InformationValue` implementation. |
| Raw bytes only | The ASDU type is recognized and `FUN`/`INF` plus payload bytes are preserved, but `getValue()` returns `null`. |
| Unknown type | The type ID is not listed in `Iec103AsduType`; ASDU and information-element raw bytes remain available for diagnostics. |

Applications can query support status with:

```java
Iec103Support support = Iec103Support.ofTypeId(typeId);
if (support.hasTypedValue()) {
    Class<? extends Iec103InformationValue> valueClass = support.getValueClass();
} else if (support.isRawBytesOnly() || support.isUnknownType()) {
    // Read raw bytes from Iec103Asdu.getRawBytes(),
    // Iec103InformationElement.getPayloadBytes(), or
    // Iec103InformationElement.getRawBytes().
}
```

See [`api-usage.md`](api-usage.md) for an end-to-end decoder example.

## Typed Value Coverage

| Type ID | ASDU type | Value class | Coverage |
| --- | --- | --- | --- |
| 1 | `TIME_TAGGED_MESSAGE` | `Iec103ProtectionEventValue` | Protection event state, quality flags, `FUN`, `INF`, and IEC103 time tag |
| 2 | `TIME_TAGGED_MESSAGE_WITH_RELATIVE_TIME` | `Iec103ProtectionEventValue` | Protection event state, quality flags, relative time, fault number, `FUN`, `INF`, and IEC103 time tag |
| 3 | `MEASURANDS_I` | `Iec103MeasuredValue` | Measurands I signed normalized value, raw quality byte, multi-object fixtures, `FUN`, `INF`, and raw element bytes |
| 5 | `IDENTIFICATION` | `Iec103IdentificationValue` | Raw-backed identification payload, US-ASCII projection, missing `FUN`/`INF` fixture, and defensive raw-byte copies |
| 9 | `MEASURANDS_II` | `Iec103MeasuredValue` | Measurands II signed normalized value, raw quality byte, boundary fixtures, `FUN`, `INF`, and raw element bytes |

## Measured Value And Identification Rules

`MEASURANDS_I` and `MEASURANDS_II` use fixed-length information
elements. After `FUN` and `INF`, the first two payload bytes are a
little-endian signed 16-bit measured value and the third payload byte is
preserved as an unsigned raw quality/status byte.

`Iec103MeasuredValue.getRawValue()` returns the signed 16-bit integer.
`getValue()` returns `rawValue / 32768.0d`, so `-32768` maps to `-1.0`
and `32767` maps to just under `1.0`. `getQuality()` returns the raw
quality/status byte; the SDK does not yet split this byte into named flags.

`IDENTIFICATION` uses one variable-length information element. The first two
bytes after the ASDU header are `FUN` and `INF`; the remaining bytes are the
identification payload. `Iec103IdentificationValue.getRawBytes()` returns the
payload only, while `Iec103InformationElement.getRawBytes()` includes `FUN`,
`INF`, and payload bytes. `getAsciiText()` is a US-ASCII projection of that
payload. Returned byte arrays are defensive copies. If a variable-length
identification ASDU is missing `FUN`/`INF`, the raw ASDU remains available but
no information element is emitted.

## Recognized Raw-only Types

These Type IDs are listed in `Iec103AsduType` and classified by `Iec103Support`,
but the decoder intentionally exposes raw information bytes only until typed
models are backed by stronger fixtures.

| Type ID | ASDU type | Coverage |
| --- | --- | --- |
| 4 | `TIME_TAGGED_MEASURANDS_WITH_RELATIVE_TIME` | `FUN`, `INF`, payload bytes, and raw element bytes are preserved with `getValue() == null`; typed measured value plus relative-time metadata is deferred |

## Deferred Categories

The following categories are intentionally outside the current enum catalog.
They therefore classify as unknown until a future slice adds explicit
`Iec103AsduType` constants and fixtures.

| Category | Current behavior | Completion condition |
| --- | --- | --- |
| Time synchronization and general interrogation | Unknown Type IDs preserve raw bytes when the ASDU envelope is structurally valid. | Add command/response value models and fixtures without adding polling or relay-session policy to the SDK. |
| Generic data and generic identification | Deferred in `0.5.0`; no explicit enum constants are published, so unknown Type IDs preserve raw bytes for caller-side handling. | Add typed or raw-only constants after representative relay traces define stable public fields. |
| Disturbance directory and disturbance transfer | Deferred in `0.5.0`; unknown Type IDs preserve raw bytes and no disturbance workflow is modeled. | Add raw-only catalog entries first, then typed file-transfer models only after real disturbance records are available. |
| Vendor-specific Type IDs | Unknown Type IDs preserve raw bytes. | Keep vendor-specific handling outside the SDK unless multiple integrations justify a stable public model. |

## Unknown Type Behavior

Unknown Type IDs are returned as `Iec103AsduType.UNKNOWN`. The decoder preserves
raw ASDU bytes and, by default, unknown information-element payload bytes. Set
`Iec103ParserConfig.builder().preserveUnknownTypePayload(false)` only when the
caller explicitly wants unknown payload bytes suppressed at the element level.

## Completion Audit Against `0.5.0` Gates

| Gate | Current status | Follow-up |
| --- | --- | --- |
| FT1.2 frame parsing covers common relay links | Covered for single-character, fixed-length, and variable-length frames with checksum validation, buffering, concatenation, max-frame limits, and recovery fixtures. | Add more real relay traces when available. |
| ASDU header exposes Type ID, VSQ, COT, common address, `FUN`, and `INF` | Covered by public `Iec103Asdu` and `Iec103InformationElement` APIs. | Add more cause/common-address fixtures if devices require two-octet common addresses. |
| Protection event values cover time-tagged and relative-time payloads | Type IDs `1` and `2` return typed `Iec103ProtectionEventValue` with quality, event state, relative time, fault number, time-tag metadata, and malformed/truncated payload fixtures. | Add real relay event catalog coverage when device traces are available. |
| Measurands I and II have typed value tests and documented conversion rules | Type IDs `3` and `9` return `Iec103MeasuredValue`; tests cover multi-object parsing, signed normalization boundaries, raw quality bytes, and defensive raw-byte copies. | Add named quality/status interpretation and engineering-unit guidance only after stable application demand. |
| Identification payloads preserve raw bytes and expose stable public fields | Type ID `5` returns `Iec103IdentificationValue` with defensive raw bytes, ASCII projection, and malformed missing-`FUN`/`INF` coverage. | Add vendor-specific identification fixtures only where fields are stable. |
| Generic data, disturbance records, and vendor-specific Type IDs are classified | Current behavior is unknown/raw-preserved; Type ID `4` is recognized raw-only. | Add explicit raw-only constants for generic and disturbance categories before any typed model claims. |
| Support matrix and usage guide describe complete, raw-only, and deferred scope | This matrix documents current typed, raw-only, unknown, and deferred categories. | Keep README, usage guide, `Iec103AsduType`, `Iec103Support`, and tests aligned. |

## Fixture Gaps

- Additional real-device protection event catalogs.
- Time-tagged measurands with relative time, currently raw-only Type ID `4`.
- Two-octet common-address fixtures if a target relay requires them.
- Representative generic data and generic identification catalog entries.
- Representative disturbance directory and disturbance-transfer catalog entries.
- Malformed generic-data payload cases after raw-only constants exist.

## Maintenance Rule

When adding an enum constant to `Iec103AsduType`, update all three places in the
same commit:

- `Iec103Support`
- `Iec103SupportMatrixTest`
- this document
