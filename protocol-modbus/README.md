# Protocol SDK - Modbus

`protocol-modbus` is the Modbus TCP and Modbus-over-UDP parser module. It is
Java 8 compatible and runtime-independent: no socket client, scheduler, Netty,
Spring, database, Redis, or message-queue dependency is included.

This module remains experimental for `0.5.0`. It may be published with the
reactor version, but Modbus stable completion is a next-phase goal and does not
block the IEC101/IEC103-focused `0.5.0` release.

## TCP Usage

The TCP decoder is stream-oriented and buffers incomplete ADUs internally.

```java
import io.github.qbsstg.protocol.core.ParseResult;
import io.github.qbsstg.protocol.modbus.ModbusAddressRange;
import io.github.qbsstg.protocol.modbus.ModbusTcpAdu;
import io.github.qbsstg.protocol.modbus.ModbusTcpStreamDecoder;

import java.util.List;

public final class ModbusTcpExample {
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
        System.out.println("functionCode=" + adu.getPdu().getFunctionCode());
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

## UDP Usage

The UDP decoder treats one datagram as one complete Modbus ADU.

```java
import io.github.qbsstg.protocol.core.ParseResult;
import io.github.qbsstg.protocol.modbus.ModbusDatagramDecoder;
import io.github.qbsstg.protocol.modbus.ModbusRegisterValues;
import io.github.qbsstg.protocol.modbus.ModbusTcpAdu;

public final class ModbusUdpExample {
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

        System.out.println("register0=" + values.getValues()[0]);
        System.out.println("register1=" + values.getValues()[1]);
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

## Baseline Coverage

Typed support:

- `0x01`: read coils request and response.
- `0x02`: read discrete inputs request and response.
- `0x03`: read holding registers request and response.
- `0x04`: read input registers request and response.
- `0x05`: write single coil request and response echo.
- `0x06`: write single register request and response echo.
- `0x0F`: write multiple coils request and response.
- `0x10`: write multiple registers request and response.
- Exception responses with function code bit `0x80` set.

Raw-only support:

- `0x17`: read/write multiple registers.
- Unknown or vendor function codes with a valid ADU envelope.

Raw bytes remain available on ADU, PDU, and typed values where useful for
diagnostics.
