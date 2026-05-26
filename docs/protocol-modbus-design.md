# Protocol Modbus Design

This note defines the first public API shape for the future
`protocol-modbus` module. The goal is a Java 8 compatible parser module for
Modbus TCP and common Modbus-over-UDP deployments without depending on Netty,
Spring, databases, message queues, or collector runtime state.

## Scope

The first release should parse Modbus Application Data Units carried over TCP
streams and UDP datagrams. It should not include serial RTU/ASCII framing,
socket clients, polling schedulers, retry policy, connection pools, or device
registry concerns.

The parser must preserve raw bytes at every layer because Modbus deployments
often include vendor-specific function codes and non-standard payloads.

## Public Model

The module should expose these core types:

| Type | Purpose |
| --- | --- |
| `ModbusTcpAdu` | MBAP header plus decoded PDU and raw ADU bytes. |
| `ModbusPdu` | Function code, typed value when supported, and raw PDU bytes. |
| `ModbusFunctionCode` | Known function-code constants with raw integer fallback. |
| `ModbusExceptionResponse` | Exception response function code and exception code. |
| `ModbusExceptionCode` | Standard exception-code constants with raw fallback. |
| `ModbusAddressRange` | Start address and quantity for read/write requests. |
| `ModbusRegisterValues` | Register-oriented response or write payload values. |
| `ModbusBitValues` | Coil/discrete-input response or write payload values. |
| `ModbusRequestResponseKey` | Transaction identifier, unit identifier, and function code for correlation. |
| `ModbusSupport` | Typed, raw-only, or unknown support status for a function code. |

`ModbusTcpAdu` should expose the MBAP fields directly:

- `transactionId`: unsigned 16-bit transaction identifier.
- `protocolId`: unsigned 16-bit protocol identifier, normally `0`.
- `length`: unsigned 16-bit byte count for `unitId + PDU`.
- `unitId`: unsigned 8-bit unit identifier.
- `pdu`: decoded `ModbusPdu`.
- `rawBytes`: complete ADU bytes.

For UDP, the same ADU model should be reused because common
Modbus-over-UDP devices use the MBAP header. A future serial module can add RTU
CRC and silent-interval framing separately without changing this API.

## Decoder Behavior

TCP decoding should use a stream decoder:

- Buffer incomplete MBAP headers until at least 7 bytes are available.
- Validate `protocolId == 0` by default.
- Use `length` to determine the complete ADU size: `6 + length` bytes.
- Reject `length < 2` because a valid ADU needs `unitId` plus at least one
  function-code byte.
- Enforce a configurable maximum ADU size to avoid unbounded buffering.
- Return success, incomplete, or error through the shared `ParseResult` API.
- Recover from malformed frames by consuming the smallest defensible prefix and
  preserving diagnostic error messages.

UDP decoding should use a datagram decoder:

- Treat one datagram as one ADU.
- Apply the same MBAP validation rules as TCP.
- Return an error when a datagram contains trailing bytes after the declared
  ADU length.
- Preserve the full datagram bytes for diagnostics.

The default parser configuration should be strict enough for interoperability
testing but allow opt-in relaxed behavior:

| Option | Default | Purpose |
| --- | --- | --- |
| `validateProtocolId` | `true` | Reject non-zero MBAP protocol identifiers. |
| `maxAduLength` | `260` | Bound TCP buffering and UDP datagram parsing. |
| `preserveUnknownFunctionPayload` | `true` | Keep unknown/vendor payload bytes. |
| `strictDatagramLength` | `true` | Reject UDP datagrams with trailing bytes. |

## Transaction Matching

The SDK should not own request tracking state. It should expose enough
information for callers to build their own correlation layer:

- `transactionId` from the MBAP header.
- `unitId` from the MBAP header.
- `functionCode` from the PDU.
- `isExceptionResponse` from the PDU.
- `requestResponseKey()` on `ModbusTcpAdu`.

For normal responses, the key should use the response function code. For
exception responses, the key should expose both the encoded exception function
code and the original function code with bit `0x80` cleared.

UDP callers can correlate request and response datagrams by the same key.
Timeouts, duplicate transaction IDs, retry windows, and outstanding request
maps belong in collector/runtime code, not this SDK module.

## Exception Responses

Modbus exception responses encode the original function code with bit `0x80`
set, followed by one exception-code byte. The parser should expose this as a
typed `ModbusExceptionResponse` with:

- `encodedFunctionCode`: raw function code from the wire.
- `originalFunctionCode`: `encodedFunctionCode & 0x7F`.
- `exceptionCode`: typed standard code when known.
- `rawExceptionCode`: unsigned 8-bit raw value.
- `rawBytes`: complete PDU bytes.

Standard exception codes for the first release:

| Code | Name |
| --- | --- |
| `0x01` | Illegal function |
| `0x02` | Illegal data address |
| `0x03` | Illegal data value |
| `0x04` | Server device failure |
| `0x05` | Acknowledge |
| `0x06` | Server device busy |
| `0x08` | Memory parity error |
| `0x0A` | Gateway path unavailable |
| `0x0B` | Gateway target device failed to respond |

Unknown exception codes must remain parseable as raw values.

## First Function-Code Coverage

The first implementation should support typed request and response payloads for
the most common process-data operations:

| Code | Name | First support |
| --- | --- | --- |
| `0x01` | Read coils | Typed request and response |
| `0x02` | Read discrete inputs | Typed request and response |
| `0x03` | Read holding registers | Typed request and response |
| `0x04` | Read input registers | Typed request and response |
| `0x05` | Write single coil | Typed request and response echo |
| `0x06` | Write single register | Typed request and response echo |
| `0x0F` | Write multiple coils | Typed request and response |
| `0x10` | Write multiple registers | Typed request and response |
| `0x17` | Read/write multiple registers | Typed request and response |

Unsupported standard or vendor function codes should parse as raw-only PDUs
when the ADU and PDU envelope is valid.

## Typed Payload Shapes

Read requests should use `ModbusAddressRange`:

- `startAddress`: unsigned 16-bit zero-based protocol address.
- `quantity`: unsigned 16-bit count.

Bit responses should use `ModbusBitValues`:

- `byteCount`: unsigned 8-bit byte count from the wire.
- `values`: ordered boolean values unpacked least-significant-bit first.
- `rawData`: packed response bytes.

Register responses should use `ModbusRegisterValues`:

- `byteCount`: unsigned 8-bit byte count from the wire.
- `values`: unsigned 16-bit register values in wire order.
- `rawData`: register payload bytes.

Write-single requests and responses should expose the echoed address and value.
Write-multiple requests should expose address range plus bit/register payloads.
Write-multiple responses should expose address range only.
Read/write multiple-register requests should expose both the read range and
write range plus the write register payload. Read/write multiple-register
responses should expose the returned register payload.

## Validation Rules

The parser should return errors for structurally invalid payloads:

- PDU shorter than one function-code byte.
- Function-specific request or response payload length mismatch.
- Response byte count inconsistent with remaining bytes.
- Register byte count not divisible by two.
- Quantity outside the standard Modbus limits for typed functions.
- Exception response payload length not equal to two bytes.

It should not reject unknown function codes solely because their payload shape
is not known. Those should be raw-only when the envelope is valid.

## Compatibility

Implementation requirements:

- Java 8 source compatible.
- No runtime or networking dependencies.
- Use shared `protocol-core` contracts.
- Keep raw bytes immutable by defensive copy.
- Preserve unsigned wire values as Java `int`.
- Avoid `java.time` in public Modbus models unless the module later needs time
  semantics.

## Implementation Order

1. Add module wiring, parser config, ADU/PDU model classes, and TCP stream
   decoder tests.
2. Add datagram decoder behavior and UDP-specific malformed-datagram tests.
3. Add typed payload parsing for function codes `0x01`, `0x02`, `0x03`,
   `0x04`, `0x05`, `0x06`, `0x0F`, and `0x10`.
4. Add exception-response modeling for all function codes.
5. Add support-matrix documentation similar to IEC104 once code exists.
