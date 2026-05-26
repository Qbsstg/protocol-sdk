# SDK 0.5.0 Release Readiness Audit

This note records the release-readiness decision for `0.5.0`. It is a
pre-release checklist only; it does not publish artifacts and does not replace
the signed Maven Central dry run required immediately before tagging.

The release candidate branch sets the Maven reactor version to `0.5.0`.

## Release Scope

`0.5.0` is the IEC101 and IEC103 completion target for this SDK line. The
release should present:

- `protocol-core` as the stable shared Java 8 parser contract module.
- `protocol-iec104` as the existing stable IEC104 parser module.
- `protocol-iec101` as the `0.5.0` completion target for practical IEC101
  FT1.2 frame and ASDU parsing scenarios.
- `protocol-iec103` as the `0.5.0` completion target for practical IEC103
  protection relay frame, event, measurand, identification, and raw fallback
  scenarios.
- `protocol-modbus` as an experimental parser module that is published with the
  same reactor version but is not a `0.5.0` completion gate.

The future runtime platform, ingestion adapters, HTTP helpers, Kafka, MQTT,
Netty sessions, databases, Redis, scheduling, and collector runtime globals are
outside the SDK `0.5.0` release scope.

## Publishing Policy

The selected `0.5.0` policy is to publish the current Maven reactor as one
versioned SDK release:

| Module | Publish at `0.5.0` | Release posture |
| --- | --- | --- |
| `protocol-sdk` | Yes | Parent POM for repository builds and dependency management. |
| `protocol-core` | Yes | Stable shared contracts. |
| `protocol-iec104` | Yes | Stable published parser, compatibility-maintained. |
| `protocol-iec101` | Yes | `0.5.0` completion target. |
| `protocol-iec103` | Yes | `0.5.0` completion target. |
| `protocol-modbus` | Yes | Experimental, non-blocking for `0.5.0`. |
| `protocol-http` | No | Planned only; no Maven module exists. |

This avoids a special release profile that excludes experimental modules. The
tradeoff is that release notes, README status labels, and module docs must say
clearly that Modbus remains experimental and will become a stability gate only
in the next major SDK phase.

## Java And CI Gates

- Public source compatibility remains Java 8.
- The root POM keeps `maven.compiler.source` and `maven.compiler.target` at
  `1.8`.
- On JDK 9 or newer, the `release-8-on-jdk9-plus` profile sets
  `maven.compiler.release=8`.
- GitHub Actions verifies every PR and `main` push on JDK 8 and JDK 21.
- Local release prep should run the Java 8 verification command before tagging.

The local machine used for this audit has JDK 8 available at:

```bash
/Library/Java/JavaVirtualMachines/zulu-8.jdk/Contents/Home
```

## Documentation Audit

| Area | Result |
| --- | --- |
| Root README module status | IEC101 and IEC103 are labeled as `0.5.0 candidate`; Modbus remains `Experimental`. |
| IEC101 docs | Support matrix documents typed, raw-only, unknown, and deferred ASDU behavior for the `0.5.0` target. |
| IEC103 docs | Support matrix and usage guide document protection events, measurands, identification, raw-only Type ID 4, and deferred generic/disturbance paths. |
| Modbus docs | README keeps the module experimental; its typed baseline remains tested but does not block `0.5.0`. |
| Runtime docs | Runtime platform and ingestion adapter plans remain separate from SDK release gating. |

## Required Checks Before Tagging

Run these checks on the release candidate commit:

```bash
mvn -q verify

JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-8.jdk/Contents/Home \
PATH=/Library/Java/JavaVirtualMachines/zulu-8.jdk/Contents/Home/bin:$PATH \
mvn -q verify

mvn -q -Pcentral-release \
  -Dgpg.skip=true \
  -Dcentral.skipPublishing=true \
  deploy
```

The Central profile command is intentionally a smoke check with publishing
disabled and signing skipped. A real release still requires:

```bash
mvn -Pcentral-release -Dcentral.skipPublishing=true clean deploy
```

That signed dry run must pass before any real Central upload.

## Audit Results On 2026-05-26

These checks passed on the release candidate branch:

| Check | Result | Note |
| --- | --- | --- |
| `mvn -q verify` | Passed | Local Maven 3.9.9 runtime reported JDK 23.0.2; this exercises the JDK 9+ `--release 8` profile path locally. |
| JDK 8 `mvn -q verify` | Passed | Used `/Library/Java/JavaVirtualMachines/zulu-8.jdk/Contents/Home`. |
| `mvn -q -Pcentral-release -Dgpg.skip=true -Dcentral.skipPublishing=true deploy` | Passed | Release profile smoke check only; no signing or publishing occurred. |
| `mvn -Pcentral-release -Dcentral.skipPublishing=true clean deploy` | Passed | Signed dry run completed; Central publishing was skipped by configuration. |

JDK 21 verification is enforced by GitHub Actions through the repository CI
matrix. The release candidate PR must pass both `Test on JDK 8` and
`Test on JDK 21` before merging.

## Current Readiness Decision

`0.5.0` can move from release preparation to tagging and publishing after the
release candidate commit passes:

- local `mvn -q verify`;
- local Java 8 `mvn -q verify`;
- Central release profile smoke check with `central.skipPublishing=true`;
- signed dry run with `central.skipPublishing=true`;
- GitHub Actions JDK 8 and JDK 21 CI.

Modbus should not block `0.5.0`. If the project later decides not to publish
experimental artifacts, add an explicit Maven release profile before tagging;
that is intentionally not part of this readiness decision.
