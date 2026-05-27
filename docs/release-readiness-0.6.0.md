# SDK 0.6.0 Release Readiness Audit

This note records the release-readiness decision for `0.6.0`. It is a
pre-release checklist only; it does not publish artifacts, does not tag a
release, and does not replace the signed Maven Central dry run required
immediately before a real upload.

The release candidate branch sets the Maven reactor version to `0.6.0`.

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
| `protocol-modbus` | Yes | `0.6.0` stable parser module for practical TCP/UDP MBAP ADU/PDU parsing. |
| `protocol-http` | No | Planned only; no Maven module exists. |

This keeps Maven coordinates simple and consistent with the `0.5.0` publishing
policy. The release candidate removes the Modbus experimental label only after
the Modbus gates below have merged and been verified on the final candidate
commit.

## Modbus Completion Gates

| Gate | Release candidate evidence | Decision |
| --- | --- | --- |
| `0.6.0` plan | PR [#50](https://github.com/Qbsstg/protocol-sdk/pull/50) merged the Modbus stable-completion target and work order. | Complete. |
| Function support matrix | PR [#51](https://github.com/Qbsstg/protocol-sdk/pull/51) added the Modbus support matrix; this release candidate aligns it with final `0.6.0` behavior. | Complete. |
| API usage guide | PR [#52](https://github.com/Qbsstg/protocol-sdk/pull/52) added TCP, UDP, correlation, exception, and raw fallback usage docs; this release candidate updates the dependency version and stability wording. | Complete. |
| Typed `0x17` support | PR [#53](https://github.com/Qbsstg/protocol-sdk/pull/53) promoted read/write multiple registers from raw-only to typed parsing. | Complete. |
| Malformed-frame fixtures | PR [#54](https://github.com/Qbsstg/protocol-sdk/pull/54) expanded TCP and UDP malformed MBAP/PDU fixtures. | Complete. |
| Quantity and byte-count validation | PR [#55](https://github.com/Qbsstg/protocol-sdk/pull/55) added standard quantity-limit and byte-count validation for typed Modbus paths. | Complete. |
| Public model immutability | PR [#56](https://github.com/Qbsstg/protocol-sdk/pull/56) locked public model final/private-final and defensive-copy contracts; PR [#59](https://github.com/Qbsstg/protocol-sdk/pull/59) added the `0x17` value-model follow-up. | Complete. |
| README stable status | This release candidate updates README module status from experimental to stable for the documented Modbus parser scope. | Complete. |
| Release notes | PR [#58](https://github.com/Qbsstg/protocol-sdk/pull/58) added `docs/release-notes-0.6.0.md`; this release candidate finalizes the notes. | Complete. |

The final readiness decision is made on this release candidate branch after all
selected Modbus gate PRs have merged into `main`.

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

## Audit Results On 2026-05-27

These checks passed on the release candidate branch:

| Check | Result | Note |
| --- | --- | --- |
| `git diff --check` | Passed | No whitespace errors in the release candidate diff. |
| `mvn -q -pl protocol-modbus -am test` | Passed | Focused Modbus module and upstream dependency tests passed. |
| `mvn -q verify` | Passed | Local Maven 3.9.9 runtime reported JDK 23.0.2; this exercises the JDK 9+ `--release 8` profile path locally. |
| JDK 8 `mvn -q verify` | Passed | Used Zulu 8 at `/Library/Java/JavaVirtualMachines/zulu-8.jdk/Contents/Home`, Java `1.8.0_452`. |
| `mvn -q -Pcentral-release -Dgpg.skip=true -Dcentral.skipPublishing=true deploy` | Passed | Release profile smoke check only; no signing or publishing occurred. |
| `mvn -Pcentral-release -Dcentral.skipPublishing=true clean deploy` | Passed | Signed dry run completed; Central publishing was skipped by configuration. |

GitHub Actions JDK 8 and JDK 21 verification must still pass on the release
candidate PR before merging.

## Current Readiness Decision

`0.6.0` can move from release preparation to tagging and publishing after the
release candidate commit passes:

- local `mvn -q -pl protocol-modbus -am test`;
- local `mvn -q verify`;
- local Java 8 `mvn -q verify`;
- Central release profile smoke check with `central.skipPublishing=true`;
- signed dry run with `central.skipPublishing=true`;
- GitHub Actions JDK 8 and JDK 21 CI.

Do not tag or publish from an unmerged local branch. Tag the final `main`
release commit only after this release candidate PR has merged and CI is green.
