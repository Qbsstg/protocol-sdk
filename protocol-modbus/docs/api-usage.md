# Modbus API Usage Guide

`protocol-modbus` is a Java 8 compatible parser module for Modbus TCP and
common Modbus-over-UDP MBAP ADUs. It is a parser SDK only: it does not open
sockets, manage polling schedules, retry requests, maintain device registries,
or store telemetry.

The module is stable in the `0.6.0` line for the documented Modbus TCP and
common Modbus-over-UDP MBAP ADU/PDU parser surface. The examples below describe
the public API shape and the caller responsibilities that stay outside this
parser SDK.

## Maven Dependency

Use the Modbus module directly:

```xml
<dependency>
    <groupId>io.github.qbsstg</groupId>
    <artifactId>protocol-modbus</artifactId>
    <version>0.6.0</version>
</dependency>
```

Most applications should not depend on the parent `protocol-sdk` POM directly.

## TCP Stream Decoding

Use one `ModbusTcpStreamDecoder` per TCP stream or session. The decoder is
stateful: it buffers incomplete MBAP headers and incomplete ADUs between
`decode()` calls.

```java
import io.github.qbsstg.protocol.core.ParseResult;
import io.github.qbsstg.protocol.modbus.ModbusAddressRange;
import io.github.qbsstg.protocol.modbus.ModbusFunctionCode;
import io.github.qbsstg.protocol.modbus.ModbusTcpAdu;
import io.github.qbsstg.protocol.modbus.ModbusTcpStreamDecoder;

import java.util.List;

public final class ModbusTcpRequestExample {
    public static void main(String[] args) {
        ModbusTcpStreamDecoder decoder = new ModbusTcpStreamDecoder();

        List<ParseResult<ModbusTcpAdu>> results = decoder.decode(bytes(
                0x00, 0x01, 0x00, 0x00, 0x00, 0x06,
                0x11, 0x03, 0x00, 0x6B, 0x00, 0x03));

        ParseResult<ModbusTcpAdu> result = results.get(0);
        if (!result.isSuccess()) {
            throw new IllegalStateException(result.getMessage());
        }

        ModbusTcpAdu adu = result.getFrame();
        ModbusAddressRange range = (ModbusAddressRange) adu.getPdu().getValue();

        System.out.println("transactionId=" + adu.getTransactionId());
        System.out.println("unitId=" + adu.getUnitId());
        System.out.println("function=" + adu.getPdu().getKnownFunctionCode());
        System.out.println("isReadHolding="
                + (adu.getPdu().getKnownFunctionCode()
                == ModbusFunctionCode.READ_HOLDING_REGISTERS));
        System.out.println("startAddress=" + range.getStartAddress());
        System.out.println("quantity=" + range.getQuantity());
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

If a TCP read splits one ADU into multiple byte chunks, feed each chunk to the
same decoder instance. A call that only completes part of a frame can return an
empty result list; the buffered bytes are retained until later input completes
the ADU.

Call `reset()` only when buffered bytes should be discarded, such as after a
TCP reconnect or after the caller decides the stream state is no longer usable.

## TCP Response Values

Register-read responses are decoded as `ModbusRegisterValues`:

```java
import io.github.qbsstg.protocol.core.ParseResult;
import io.github.qbsstg.protocol.modbus.ModbusRegisterValues;
import io.github.qbsstg.protocol.modbus.ModbusTcpAdu;
import io.github.qbsstg.protocol.modbus.ModbusTcpStreamDecoder;

import java.util.List;

public final class ModbusRegisterResponseExample {
    public static void main(String[] args) {
        ModbusTcpStreamDecoder decoder = new ModbusTcpStreamDecoder();

        List<ParseResult<ModbusTcpAdu>> results = decoder.decode(bytes(
                0x00, 0x01, 0x00, 0x00, 0x00, 0x09,
                0x11, 0x03, 0x06, 0x02, 0x2B, 0x00, 0x00, 0x00, 0x64));

        ParseResult<ModbusTcpAdu> result = results.get(0);
        if (!result.isSuccess()) {
            throw new IllegalStateException(result.getMessage());
        }

        ModbusRegisterValues values =
                (ModbusRegisterValues) result.getFrame().getPdu().getValue();

        int[] registers = values.getValues();
        System.out.println("byteCount=" + values.getByteCount());
        System.out.println("firstRegister=" + registers[0]);
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

Coil and discrete-input responses are decoded as `ModbusBitValues`. Bits are
unpacked least-significant-bit first within each byte. Because a Modbus bit
response does not carry the original requested quantity, the current parser
returns `byteCount * 8` boolean values.

## UDP Datagram Decoding

Use `ModbusDatagramDecoder` when one received datagram contains one MBAP ADU.
The decoder is stateless.

```java
import io.github.qbsstg.protocol.core.ParseResult;
import io.github.qbsstg.protocol.modbus.ModbusDatagramDecoder;
import io.github.qbsstg.protocol.modbus.ModbusRegisterValues;
import io.github.qbsstg.protocol.modbus.ModbusTcpAdu;

public final class ModbusUdpDatagramExample {
    public static void main(String[] args) {
        ModbusDatagramDecoder decoder = new ModbusDatagramDecoder();

        ParseResult<ModbusTcpAdu> result = decoder.decode(bytes(
                0x00, 0x01, 0x00, 0x00, 0x00, 0x07,
                0x11, 0x03, 0x04, 0x02, 0x2B, 0x00, 0x64));

        if (!result.isSuccess()) {
            throw new IllegalStateException(result.getMessage());
        }

        ModbusRegisterValues values =
                (ModbusRegisterValues) result.getFrame().getPdu().getValue();

        System.out.println("unitId=" + result.getFrame().getUnitId());
        System.out.println("registerCount=" + values.getValues().length);
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

By default, UDP decoding is strict: extra trailing bytes after the MBAP-declared
ADU length produce `ParseResult.error()`. If a deployment needs to accept
datagrams with trailing bytes, configure `strictDatagramLength` as `false`:

```java
ModbusDatagramDecoder decoder = new ModbusDatagramDecoder(
        new ModbusParserConfig(true, ModbusParserConfig.DEFAULT_MAX_ADU_LENGTH, false));
```

## Request And Response Correlation

The SDK does not keep an outstanding request map. Caller code should track
request/response state in the runtime layer.

`ModbusTcpAdu#getRequestResponseKey()` exposes:

- transaction id
- unit id
- original function code

For exception responses, the key uses the original function code with bit
`0x80` cleared, so a response with encoded function `0x83` correlates to a
normal `0x03` request.

```java
import io.github.qbsstg.protocol.modbus.ModbusRequestResponseKey;
import io.github.qbsstg.protocol.modbus.ModbusTcpAdu;

import java.util.HashMap;
import java.util.Map;

public final class ModbusCorrelationExample {
    private final Map<ModbusRequestResponseKey, Long> sentAt =
            new HashMap<ModbusRequestResponseKey, Long>();

    public void rememberRequest(ModbusTcpAdu request) {
        sentAt.put(request.getRequestResponseKey(), Long.valueOf(System.currentTimeMillis()));
    }

    public Long findRequestTime(ModbusTcpAdu response) {
        return sentAt.get(response.getRequestResponseKey());
    }
}
```

Timeouts, duplicate transaction ids, retry windows, connection ownership, and
polling intervals belong in runtime code, not in this SDK module.

## Exception Responses

Exception responses are returned as successful frame parses when the Modbus PDU
is structurally valid. Check `ModbusPdu#isExceptionResponse()` before casting
the value.

```java
import io.github.qbsstg.protocol.core.ParseResult;
import io.github.qbsstg.protocol.modbus.ModbusExceptionResponse;
import io.github.qbsstg.protocol.modbus.ModbusPdu;
import io.github.qbsstg.protocol.modbus.ModbusTcpAdu;
import io.github.qbsstg.protocol.modbus.ModbusTcpStreamDecoder;

import java.util.List;

public final class ModbusExceptionExample {
    public static void main(String[] args) {
        ModbusTcpStreamDecoder decoder = new ModbusTcpStreamDecoder();

        List<ParseResult<ModbusTcpAdu>> results = decoder.decode(bytes(
                0x00, 0x01, 0x00, 0x00, 0x00, 0x03,
                0x11, 0x83, 0x02));

        ParseResult<ModbusTcpAdu> result = results.get(0);
        if (!result.isSuccess()) {
            throw new IllegalStateException(result.getMessage());
        }

        ModbusPdu pdu = result.getFrame().getPdu();
        if (pdu.isExceptionResponse()) {
            ModbusExceptionResponse exception =
                    (ModbusExceptionResponse) pdu.getValue();

            System.out.println("encodedFunction=" + exception.getEncodedFunctionCode());
            System.out.println("originalFunction=" + exception.getOriginalFunctionCode());
            System.out.println("exceptionCode=" + exception.getExceptionCode());
            System.out.println("rawExceptionCode=" + exception.getRawExceptionCode());
        }
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

If the exception code is not one of the modeled standard codes,
`getExceptionCode()` returns `null` and `getRawExceptionCode()` still preserves
the unsigned wire value.

## Raw Fallback

Unknown or vendor-specific function codes are not rejected solely because their
payload shape is not modeled. When the MBAP ADU and PDU envelope is valid, the
parser returns `ModbusRawValue` and preserves the function payload bytes after
the function-code byte.

```java
import io.github.qbsstg.protocol.core.ParseResult;
import io.github.qbsstg.protocol.modbus.ModbusRawValue;
import io.github.qbsstg.protocol.modbus.ModbusSupportStatus;
import io.github.qbsstg.protocol.modbus.ModbusTcpAdu;
import io.github.qbsstg.protocol.modbus.ModbusTcpStreamDecoder;

import java.util.List;

public final class ModbusRawFallbackExample {
    public static void main(String[] args) {
        ModbusTcpStreamDecoder decoder = new ModbusTcpStreamDecoder();

        List<ParseResult<ModbusTcpAdu>> results = decoder.decode(bytes(
                0x00, 0x09, 0x00, 0x00, 0x00, 0x04,
                0x11, 0x41, 0xAA, 0x55));

        ParseResult<ModbusTcpAdu> result = results.get(0);
        if (!result.isSuccess()) {
            throw new IllegalStateException(result.getMessage());
        }

        if (result.getFrame().getPdu().getSupport().getStatus()
                == ModbusSupportStatus.UNKNOWN) {
            ModbusRawValue raw = (ModbusRawValue) result.getFrame().getPdu().getValue();
            System.out.println("rawPayloadLength=" + raw.getRawBytes().length);
        }
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

Known but intentionally deferred function codes use `ModbusRawValue` with
`RAW_ONLY` support when they are explicitly modeled. Unknown or vendor-specific
function codes also use `ModbusRawValue`, with `UNKNOWN` support.

## Error Handling

Transport or PDU structure errors are returned as `ParseResult.error()` with a
diagnostic message and consumed byte count. Examples include:

- invalid non-zero MBAP protocol id when protocol id validation is enabled
- MBAP length smaller than `unitId + functionCode`
- ADU length above the configured maximum
- UDP datagram trailing bytes when strict datagram length is enabled
- function-specific PDU length mismatch
- response byte count mismatch
- odd register byte count
- standard quantity-limit violations for typed read/write paths
- malformed exception response length

Use `ParseResult.isIncomplete()` for short UDP datagrams or incomplete TCP
stream chunks. Use `ParseResult.isError()` for malformed input that the parser
rejected.

## Current Coverage Boundary

The current typed parser surface covers common process-data function codes:

- `0x01` read coils
- `0x02` read discrete inputs
- `0x03` read holding registers
- `0x04` read input registers
- `0x05` write single coil
- `0x06` write single register
- `0x0F` write multiple coils
- `0x10` write multiple registers
- `0x17` read/write multiple registers

Other standard or vendor-specific function codes are preserved as raw payloads
when the envelope is valid.
