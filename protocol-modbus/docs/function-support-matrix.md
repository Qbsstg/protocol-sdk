# Modbus Function Support Matrix

This matrix records the current `protocol-modbus` parser behavior. It describes
the implemented Modbus TCP and common Modbus-over-UDP MBAP ADU parser surface;
it is not a claim of full Modbus ecosystem coverage.

`protocol-modbus` is still experimental in `0.5.0`. The `0.6.0` target is to
use this matrix as the starting point for promoting the module to a stable
TCP/UDP parser module.

## Status Legend

| Status | Meaning |
| --- | --- |
| `TYPED` | The function code is known and the parser returns a typed `ModbusPduValue` for the supported request or response shape. |
| `RAW_ONLY` | The function code is known, but the parser preserves the payload as `ModbusRawValue` instead of interpreting it. |
| `UNKNOWN` | The function code is not modeled by `ModbusFunctionCode`; the parser preserves the payload as `ModbusRawValue` when the ADU/PDU envelope is valid. |
| `ERROR` | The parser rejects structurally invalid payloads with `ParseResult.error()`. |
| `DEFERRED` | The behavior is intentionally outside the current typed parser surface. |

Exception responses are a special case: the exception envelope is typed as
`ModbusExceptionResponse` when the PDU length is valid, while the normalized
original function code still determines the `ModbusSupport` classification.

## Transport Envelope Support

| Area | Current behavior | Notes for `0.6.0` |
| --- | --- | --- |
| TCP MBAP stream | `ModbusTcpStreamDecoder` buffers incomplete ADUs, decodes concatenated ADUs, validates protocol id by default, enforces MBAP length, and applies the configured max ADU length. | Add broader malformed-frame fixtures before claiming stable coverage. |
| UDP MBAP datagram | `ModbusDatagramDecoder` treats one datagram as one ADU, rejects short datagrams as incomplete, and rejects trailing bytes by default. | Keep strict trailing-byte behavior documented; relaxed trailing bytes remain configuration-driven. |
| Raw bytes | `ModbusTcpAdu`, `ModbusPdu`, typed values with payload data, exception responses, and raw values preserve defensive copies of wire bytes where applicable. | Continue preserving raw bytes for diagnostics and vendor-specific handling. |
| RTU/ASCII serial framing | Not supported. | Keep out of SDK `0.6.0` unless a separate serial-framing module is explicitly planned. |
| Network clients and polling | Not supported. | Belongs in a runtime/collector layer, not this parser SDK. |

## Function-Code Matrix

The parser distinguishes request and response payloads by PDU shape. It does
not keep request-correlation state, so response values are decoded from the
response payload alone.

| Code | Name | Current support | Request value | Response value | Current validation and gaps |
| --- | --- | --- | --- | --- | --- |
| `0x01` | Read coils | `TYPED` | `ModbusAddressRange` for 5-byte requests. | `ModbusBitValues` for byte-count responses. | Validates response byte count. Does not yet validate standard request quantity limits. Response bit arrays are unpacked to `byteCount * 8` bits because the response does not carry requested quantity. |
| `0x02` | Read discrete inputs | `TYPED` | `ModbusAddressRange` for 5-byte requests. | `ModbusBitValues` for byte-count responses. | Same behavior and gaps as `0x01`. |
| `0x03` | Read holding registers | `TYPED` | `ModbusAddressRange` for 5-byte requests. | `ModbusRegisterValues` for byte-count responses. | Validates response byte count and even register payload length. Does not yet validate standard request quantity limits. |
| `0x04` | Read input registers | `TYPED` | `ModbusAddressRange` for 5-byte requests. | `ModbusRegisterValues` for byte-count responses. | Same behavior and gaps as `0x03`. |
| `0x05` | Write single coil | `TYPED` | `ModbusWriteSingleValue`. | `ModbusWriteSingleValue` echo. | Validates 5-byte PDU length. Does not yet reject non-standard coil values outside `0x0000` and `0xFF00`. |
| `0x06` | Write single register | `TYPED` | `ModbusWriteSingleValue`. | `ModbusWriteSingleValue` echo. | Validates 5-byte PDU length. |
| `0x0F` | Write multiple coils | `TYPED` | `ModbusWriteMultipleBitsValue` for payload requests. | `ModbusAddressRange` for 5-byte response echoes. | Validates PDU length against byte count. Does not yet validate standard quantity limits or byte count against `ceil(quantity / 8)`. |
| `0x10` | Write multiple registers | `TYPED` | `ModbusWriteMultipleRegistersValue` for payload requests. | `ModbusAddressRange` for 5-byte response echoes. | Validates PDU length against byte count and rejects odd register byte counts. Does not yet validate standard quantity limits or byte count against `quantity * 2`. |
| `0x17` | Read/write multiple registers | `RAW_ONLY` | `ModbusRawValue`. | `ModbusRawValue`. | Known function code, but payload is intentionally not interpreted in the current baseline. This is the main `0.6.0` typed-coverage gap. |
| Unknown/vendor codes | Vendor-specific or deferred functions | `UNKNOWN` | `ModbusRawValue`. | `ModbusRawValue`. | The parser preserves payload bytes when the ADU and PDU envelope is valid. Typed behavior is deferred until the code is explicitly modeled. |

## Deferred Standard Function Codes

The following standard function-code families are not modeled as typed values
yet. When they appear without the exception bit set, they currently parse as
`UNKNOWN` with `ModbusRawValue` if the MBAP ADU and PDU envelope is valid.

| Code | Name | Current behavior |
| --- | --- | --- |
| `0x07` | Read exception status | `UNKNOWN` raw payload. |
| `0x08` | Diagnostics | `UNKNOWN` raw payload. |
| `0x0B` | Get comm event counter | `UNKNOWN` raw payload. |
| `0x0C` | Get comm event log | `UNKNOWN` raw payload. |
| `0x11` | Report server id | `UNKNOWN` raw payload. |
| `0x14` | Read file record | `UNKNOWN` raw payload. |
| `0x15` | Write file record | `UNKNOWN` raw payload. |
| `0x16` | Mask write register | `UNKNOWN` raw payload. |
| `0x18` | Read FIFO queue | `UNKNOWN` raw payload. |
| `0x2B` | Encapsulated interface transport | `UNKNOWN` raw payload. |

These functions do not block `0.6.0` unless the stable release claim changes
from common process-data parsing to broader Modbus application coverage.

## Exception Response Matrix

| Area | Current behavior |
| --- | --- |
| Encoded function code | Any function code with bit `0x80` set is parsed as an exception response. |
| PDU length | Exception responses must be exactly two bytes: encoded function code plus exception code. Other lengths return `ERROR`. |
| Original function code | Exposed as `encodedFunctionCode & 0x7F`. |
| Known original function | Exposed as `ModbusFunctionCode` when modeled, otherwise `null`. |
| Exception code | Standard exception codes are exposed as `ModbusExceptionCode`; unknown codes keep `rawExceptionCode` and expose `null` for the typed enum. |
| Raw bytes | The full exception PDU is preserved by `ModbusExceptionResponse`. |

Typed exception-code coverage:

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

## `0.6.0` Follow-Up Items

Use this matrix to drive the next Modbus stabilization tasks:

1. Promote `0x17` read/write multiple registers from `RAW_ONLY` to typed
   request and response parsing.
2. Add standard quantity-limit validation for typed read and write functions.
3. Add byte-count validation tied to quantity for `0x0F` and `0x10` requests.
4. Add write-single-coil value validation for `0x05`.
5. Expand malformed MBAP and malformed PDU fixtures for both TCP and UDP
   decoders.
6. Add a public Modbus usage guide that explains TCP stream decoding, UDP
   datagram decoding, request/response correlation, exception responses, and
   raw fallback.
