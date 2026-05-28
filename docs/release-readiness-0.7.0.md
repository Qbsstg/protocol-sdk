# SDK 0.7.0 Release Readiness Audit

This note records the pre-release readiness decision for `0.7.0`. It does not
publish artifacts and does not replace the final release branch, Java 8
verification, signed dry run, tag, or Maven Central upload.

The later release commit should set the Maven reactor version to `0.7.0`.

## Release Scope

`0.7.0` is the IEC104 hardening target after the published `0.6.0` Modbus
stable release. The release should present:

- `protocol-core` as the stable shared Java 8 parser contract module.
- `protocol-iec104` as the `0.7.0` hardening target with current audit,
  fixture-backed diagnostics, raw-only boundaries, and final API behavior docs.
- `protocol-iec101` as a compatibility-maintained module after its `0.5.0`
  completion release.
- `protocol-iec103` as a compatibility-maintained module after its `0.5.0`
  completion release.
- `protocol-modbus` as a compatibility-maintained module after its `0.6.0`
  stable release.

The future runtime platform, ingestion adapters, HTTP helpers, Kafka, MQTT,
Netty sessions, databases, Redis, scheduling, polling loops, retry policy,
device registry, and collector runtime globals are outside the SDK `0.7.0`
release scope.

## Publishing Policy

The selected `0.7.0` policy is to publish the current Maven reactor as one
versioned SDK release:

| Module | Publish at `0.7.0` | Release posture |
| --- | --- | --- |
| `protocol-sdk` | Yes | Parent POM for repository builds and dependency management. |
| `protocol-core` | Yes | Stable shared Java 8 parser contracts. |
| `protocol-iec104` | Yes | `0.7.0` hardening target for practical IEC104 parser use. |
| `protocol-iec101` | Yes | Published parser, compatibility-maintained after `0.5.0`. |
| `protocol-iec103` | Yes | Published parser, compatibility-maintained after `0.5.0`. |
| `protocol-modbus` | Yes | Stable parser module, compatibility-maintained after `0.6.0`. |
| `protocol-http` | No | Planned only; no Maven module exists. |

This keeps Maven coordinates simple and consistent with the `0.5.0` and
`0.6.0` publishing policies. The release is about IEC104 parser hardening; it
does not add runtime ingestion dependencies or a formal IEC104 conformance
claim.

## IEC104 Hardening Gates

| Gate | Release evidence | Decision |
| --- | --- | --- |
| `0.7.0` plan | PR [#62](https://github.com/Qbsstg/protocol-sdk/pull/62) merged the IEC104 hardening target, gates, and work order. | Complete. |
| Raw-only catalog fixtures | PR [#63](https://github.com/Qbsstg/protocol-sdk/pull/63) added direct fixtures for `M_EI_NA_1` and file-transfer Type IDs `120` through `126`. | Complete. |
| VSQ/SQ boundary behavior | PR [#64](https://github.com/Qbsstg/protocol-sdk/pull/64) added representative typed and raw-only sequence/non-sequence fixtures. | Complete. |
| Strict malformed ASDU diagnostics | PR [#65](https://github.com/Qbsstg/protocol-sdk/pull/65) covered strict truncated-header, address, information-element, sequential-element, recovery, and raw-only permissive behavior. | Complete. |
| Quality and time tags | PR [#66](https://github.com/Qbsstg/protocol-sdk/pull/66) added representative quality descriptor and `CP56Time2a` edge-case fixtures. | Complete. |
| API behavior docs | PR [#67](https://github.com/Qbsstg/protocol-sdk/pull/67) documented decoder lifecycle, strict/permissive behavior, raw fallback, support lookup, quality/time fields, and SDK/runtime boundaries. | Complete. |
| Cause diagnostics | `Iec104CauseOfTransmissionTest` covers counter-interrogation return causes `37` through `41`, diagnostic causes `44` through `47`, and unknown fallback behavior. | Complete. |
| Completeness audit | `protocol-iec104/docs/iec104-completeness-audit.md` reflects the current post-`0.6.0` IEC104 parser state and remaining non-goals. | Complete. |

## Java And CI Gates

- Public source compatibility remains Java 8.
- The root POM keeps `maven.compiler.source` and `maven.compiler.target` at
  `1.8`.
- On JDK 9 or newer, the `release-8-on-jdk9-plus` profile sets
  `maven.compiler.release=8`.
- GitHub Actions must verify the release commit on JDK 8 and JDK 21.
- Local release prep should run Java 8 verification before tagging.

The local Java 8 path used for earlier release work is:

```bash
/Library/Java/JavaVirtualMachines/zulu-8.jdk/Contents/Home
```

## Required Checks Before Tagging

Run these checks on the final `0.7.0` release commit after all selected gate
PRs have merged:

```bash
mvn -q -pl protocol-iec104 -am test

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

## Documentation Branch Checks On 2026-05-28

These checks passed on the readiness documentation branch:

| Check | Result | Note |
| --- | --- | --- |
| `git diff --check` | Passed | No whitespace errors in the documentation diff. |
| `mvn -q -pl protocol-iec104 -am test` | Passed | Focused IEC104 module and upstream dependency tests passed. |
| `mvn -q verify` | Passed | Full reactor verification passed on the local JDK used by this workspace. |

These checks do not replace the final release-branch Java 8 verification,
Central profile smoke check, signed dry run, or GitHub Actions release PR
checks.

## Current Readiness Decision

`0.7.0` can move from hardening work to release preparation after this readiness
note and the release notes merge, then the release branch should:

- update the Maven reactor version to `0.7.0`;
- run the required local, Java 8, Central profile, and signed dry-run checks;
- pass GitHub Actions on JDK 8 and JDK 21;
- tag and publish only after explicit release approval.
