# Protocol SDK - IEC103

`protocol-iec103` is the IEC 60870-5-103 parser module. It is Java 8
compatible and depends only on `protocol-core` at runtime.

## Usage

```java
import io.github.qbsstg.protocol.core.ParseResult;
import io.github.qbsstg.protocol.iec103.Iec103Frame;
import io.github.qbsstg.protocol.iec103.Iec103InformationElement;
import io.github.qbsstg.protocol.iec103.Iec103ProtectionEventValue;
import io.github.qbsstg.protocol.iec103.Iec103StreamDecoder;

import java.util.List;

Iec103StreamDecoder decoder = new Iec103StreamDecoder();
List<ParseResult<Iec103Frame>> results = decoder.decode(bytesFromRelayLink);

for (ParseResult<Iec103Frame> result : results) {
    if (result.isSuccess() && result.getFrame().getAsdu() != null) {
        Iec103InformationElement element =
                result.getFrame().getAsdu().getInformationElements().get(0);
        Iec103ProtectionEventValue value = (Iec103ProtectionEventValue) element.getValue();
        boolean on = value.isOn();
    }
}
```

Use one `Iec103StreamDecoder` per relay link/session because the decoder
buffers incomplete frames internally.

For a fuller example, see [`docs/api-usage.md`](docs/api-usage.md).

## Baseline Scope

Frame support:

- Single-character acknowledgement `0xE5`.
- Fixed-length FT1.2 frames.
- Variable-length FT1.2 frames carrying ASDUs.
- One- and two-octet link addresses.
- Configurable one- and two-octet common addresses.
- Checksum validation, split-payload buffering, concatenated frames, maximum
  frame-size checks, and noise recovery.

ASDU support:

- ASDU header parsing for Type ID, VSQ, cause of transmission, and common
  address.
- Information element parsing for `FUN`, `INF`, payload bytes, and raw bytes.
- Typed protection events: `TIME_TAGGED_MESSAGE` and
  `TIME_TAGGED_MESSAGE_WITH_RELATIVE_TIME`.
- Typed measured values: `MEASURANDS_I` and `MEASURANDS_II`.
- Typed raw-backed identification: `IDENTIFICATION`.
- Recognized raw-only: `TIME_TAGGED_MEASURANDS_WITH_RELATIVE_TIME`.
- Raw-byte fallback for unknown Type IDs.

The detailed support matrix and `0.5.0` completion audit are maintained in
[`docs/asdu-support-matrix.md`](docs/asdu-support-matrix.md).

This module does not include relay polling/session scheduling, retry policy,
disturbance-transfer orchestration, serial-port handling, Netty, Spring,
databases, Redis, MQTT, Kafka, or message queues.
