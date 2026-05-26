# Protocol SDK - IEC101

`protocol-iec101` is the IEC 60870-5-101 parser module. It is Java 8
compatible and depends only on `protocol-core` at runtime.

## Usage

```java
import io.github.qbsstg.protocol.core.ParseResult;
import io.github.qbsstg.protocol.iec101.Iec101Frame;
import io.github.qbsstg.protocol.iec101.Iec101InformationObject;
import io.github.qbsstg.protocol.iec101.Iec101ParserConfig;
import io.github.qbsstg.protocol.iec101.Iec101SinglePointValue;
import io.github.qbsstg.protocol.iec101.Iec101StreamDecoder;

import java.util.List;

Iec101StreamDecoder decoder = new Iec101StreamDecoder(Iec101ParserConfig.defaultUnbalanced());
List<ParseResult<Iec101Frame>> results = decoder.decode(bytesFromSerialLink);

for (ParseResult<Iec101Frame> result : results) {
    if (result.isSuccess() && result.getFrame().getAsdu() != null) {
        Iec101InformationObject object = result.getFrame().getAsdu().getInformationObjects().get(0);
        Iec101SinglePointValue value = (Iec101SinglePointValue) object.getValue();
        boolean on = value.isOn();
    }
}
```

Use one `Iec101StreamDecoder` per link/session because the decoder buffers
incomplete frames internally.

## Baseline Scope

Frame support:

- Single-character acknowledgement `0xE5`.
- Fixed-length FT1.2 frames.
- Variable-length FT1.2 frames carrying ASDUs.
- One- and two-octet link addresses.
- Checksum validation, split-payload buffering, concatenated frames, and noise
  recovery.

ASDU support:

- Configurable cause-of-transmission, common-address, and information-object
  address lengths.
- Typed first set: `M_SP_NA_1`, `M_DP_NA_1`, `M_ME_NA_1`, `M_ME_NB_1`,
  `M_ME_NC_1`, `C_SC_NA_1`, `C_DC_NA_1`, and `C_IC_NA_1`.
- Selected time-tagged process values: `M_SP_TA_1`, `M_DP_TA_1`,
  `M_ME_TA_1`, `M_ME_TB_1`, `M_ME_TC_1`, `M_SP_TB_1`, `M_DP_TB_1`,
  `M_ME_TD_1`, `M_ME_TE_1`, and `M_ME_TF_1`.
- Station-service commands: `C_CI_NA_1`, `C_RD_NA_1`, `C_CS_NA_1`,
  `C_RP_NA_1`, and `C_CD_NA_1`.
- Raw-byte fallback for unknown Type IDs.

The detailed support matrix and `0.5.0` completion audit are maintained in
[`docs/asdu-support-matrix.md`](docs/asdu-support-matrix.md).

This module does not include serial-port handling, polling/session scheduling,
retry policy, Netty, Spring, databases, Redis, or message queues.
