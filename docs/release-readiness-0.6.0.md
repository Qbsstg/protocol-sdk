# SDK 0.6.0 Release Readiness Audit

This note records the release-readiness decision for `0.6.0`. It is a
pre-release checklist only; it does not publish artifacts, does not tag a
release, and does not replace the signed Maven Central dry run required
immediately before a real upload.

## Release Scope

`0.6.0` is the Modbus stable-completion target after the published `0.5.0`
IEC101 and IEC103 completion release. The release should present:

- `protocol-core` as the stable shared Java 8 parser contract module.
- `protocol-iec104` as the existing stable IEC104 parser module.
- `protocol-iec101` as a compatibility-maintained module after its `0.5.0`
  completion release.
- `protocol-iec103` as a compatibility-maintained module after its `0.5.0`
  completion release.
- `protocol-modbus` as the `0.6.0` completion target for practical Modbus TCP
  and common Modbus-over-UDP MBAP ADU/PDU parsing scenarios.

The future runtime platform, ingestion adapters, HTTP helpers, Kafka, MQTT,
Netty sessions, databases, Redis, scheduling, polling loops, retry policy,
device registry, and collector runtime globals are outside the SDK `0.6.0`
release scope.

## Publishing Policy

The selected `0.6.0` policy is to publish the current Maven reactor as one
versioned SDK release:

| Module | Publish at `0.6.0` | Release posture |
| --- | --- | --- |
| `protocol-sdk` | Yes | Parent POM for repository builds and dependency management. |
| `protocol-core` | Yes | Stable shared Java 8 parser contracts. |
| `protocol-iec104` | Yes | Stable published parser, compatibility-maintained. |
| `protocol-iec101` | Yes | Published parser, compatibility-maintained after `0.5.0`. |
| `protocol-iec103` | Yes | Published parser, compatibility-maintained after `0.5.0`. |
| `protocol-modbus` | Yes | `0.6.0` stable-completion target after gates pass. |
| `protocol-http` | No | Planned only; no Maven module exists. |

This keeps Maven coordinates simple and consistent with the `0.5.0` publishing
policy. The README and module docs should remove the Modbus experimental label
only after the Modbus gates below are merged and verified on the final release
candidate.

## Modbus Completion Gates

| Gate | Current status on 2026-05-27 | Required before tagging |
| --- | --- | --- |
| `0.6.0` plan | Draft PR [#50](https://github.com/Qbsstg/protocol-sdk/pull/50) defines the Modbus stable-completion target and work order. | Merge the plan or an equivalent release-scope document. |
| Function support matrix | Draft PR [#51](https://github.com/Qbsstg/protocol-sdk/pull/51) adds the Modbus support matrix. | Merge and keep it aligned with final code behavior. |
| API usage guide | Draft PR [#52](https://github.com/Qbsstg/protocol-sdk/pull/52) adds TCP, UDP, correlation, exception, and raw fallback usage docs. | Merge and update examples if public APIs change. |
| Typed `0x17` support | Draft PR [#53](https://github.com/Qbsstg/protocol-sdk/pull/53) promotes read/write multiple registers from raw-only to typed parsing. | Merge or explicitly defer `0x17` before using a stable Modbus label. |
| Malformed-frame fixtures | Draft PR [#54](https://github.com/Qbsstg/protocol-sdk/pull/54) expands TCP and UDP malformed MBAP/PDU fixtures. | Merge and keep recovery behavior documented. |
| Quantity and byte-count validation | Draft PR [#55](https://github.com/Qbsstg/protocol-sdk/pull/55) adds standard quantity-limit and byte-count validation. | Merge and verify final PDU validation behavior. |
| Public model immutability | Draft PR [#56](https://github.com/Qbsstg/protocol-sdk/pull/56) locks public model final/private-final and defensive-copy contracts. | Merge and keep Java 8 compatibility. |
| README stable status | Not yet updated on `main`; Modbus remains experimental. | Update only after the gates above are merged and verified. |
| Release notes | Not yet prepared for `0.6.0`. | Add `docs/release-notes-0.6.0.md` before release prep. |

The current open PRs are useful gate evidence, but they are not a final release
candidate. The final readiness decision must be made on one merged release
candidate commit.

## Java And CI Gates

- Public source compatibility remains Java 8.
- The root POM keeps `maven.compiler.source` and `maven.compiler.target` at
  `1.8`.
- On JDK 9 or newer, the `release-8-on-jdk9-plus` profile sets
  `maven.compiler.release=8`.
- GitHub Actions must verify the release candidate on JDK 8 and JDK 21.
- Local release prep should run Java 8 verification before tagging.

The local Java 8 path used for earlier release work is:

```bash
/Library/Java/JavaVirtualMachines/zulu-8.jdk/Contents/Home
```

## Required Checks Before Tagging

Run these checks on the final `0.6.0` release candidate commit after all
selected gate PRs have merged:

```bash
mvn -q -pl protocol-modbus -am test

mvn -q verify

JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-8.jdk/Contents/Home \
PATH=/Library/Java/JavaVirtualMachines/zulu-8.jdk/Contents/Home/bin:$PATH \
mvn -q verify

mvn -q -Pcentral-release \
  -Dgpg.skip=true \
  -Dcentral.skipPublishing=true \
  deploy
```

The Central profile command above is intentionally a smoke check with
publishing disabled and signing skipped. A real release still requires a signed
dry run:

```bash
mvn -Pcentral-release -Dcentral.skipPublishing=true clean deploy
```

That signed dry run must pass before any real Central upload.

## Current Readiness Decision

`0.6.0` is not ready to tag or publish on 2026-05-27.

The next readiness step is to merge the Modbus gate PRs, resolve any conflicts
between them, update README/module status from experimental to stable only when
the merged behavior supports that claim, prepare `docs/release-notes-0.6.0.md`,
and then run the full release-candidate verification set above.

Do not publish `0.6.0` from the current `main` branch state. The current `main`
branch still represents the published `0.5.0` line where Modbus is experimental.
