# IEC103 API Usage

`protocol-iec103` parses IEC 60870-5-103 FT1.2 link frames and exposes the ASDU
header plus `FUN` and `INF` information elements. The module is a parser only:
serial ports, TCP bridges, polling schedules, retry policy, databases, and
collector runtime state belong outside the SDK.

## Stream Decoder

Use one decoder per relay link session. The decoder buffers incomplete frames,
so callers can feed bytes directly from serial reads or TCP-to-serial bridge
packets.

```java
import io.github.qbsstg.protocol.core.ParseResult;
import io.github.qbsstg.protocol.iec103.Iec103Frame;
import io.github.qbsstg.protocol.iec103.Iec103InformationElement;
import io.github.qbsstg.protocol.iec103.Iec103ProtectionEventValue;
import io.github.qbsstg.protocol.iec103.Iec103StreamDecoder;

import java.util.List;

public final class Iec103Example {
    public static void main(String[] args) {
        Iec103StreamDecoder decoder = new Iec103StreamDecoder();

        List<ParseResult<Iec103Frame>> results = decoder.decode(bytes(
                0x68, 0x0D, 0x0D, 0x68,
                0x08, 0x01,
                0x01, 0x01, 0x01, 0x01,
                0x10, 0x01, 0xD2, 0xE8, 0x03, 0x15, 0x10,
                0x00, 0x16));

        ParseResult<Iec103Frame> result = results.get(0);
        if (!result.isSuccess()) {
            throw new IllegalStateException(result.getMessage());
        }

        Iec103InformationElement element =
                result.getFrame().getAsdu().getInformationElements().get(0);
        Iec103ProtectionEventValue value = (Iec103ProtectionEventValue) element.getValue();

        System.out.println("type=" + result.getFrame().getAsdu().getType());
        System.out.println("fun=" + element.getFunctionType());
        System.out.println("inf=" + element.getInformationNumber());
        System.out.println("on=" + value.isOn());
        System.out.println("minute=" + value.getTimeTag().getMinute());
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

## First Typed Values

The first baseline returns typed values for:

| Type ID | Value class |
| --- | --- |
| `1` | `Iec103ProtectionEventValue` |
| `2` | `Iec103ProtectionEventValue` |
| `3` | `Iec103MeasuredValue` |
| `5` | `Iec103IdentificationValue` |
| `9` | `Iec103MeasuredValue` |

Unknown Type IDs and raw-only Type IDs still preserve raw ASDU and information
element bytes for diagnostics and vendor-specific handling.

Measured values expose both the signed 16-bit raw value and the normalized
`rawValue / 32768.0d` value. The measured-value quality/status byte is
available as a raw unsigned byte through `Iec103MeasuredValue.getQuality()`.

Identification values expose the variable-length payload bytes through
`Iec103IdentificationValue.getRawBytes()` and a US-ASCII projection through
`getAsciiText()`. The surrounding `Iec103InformationElement` still exposes the
full `FUN`/`INF` plus payload bytes.

For the detailed typed, raw-only, unknown, and deferred coverage audit, see
[`asdu-support-matrix.md`](asdu-support-matrix.md).
