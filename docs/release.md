# Release Guide

This project publishes Java 8 compatible artifacts to Maven Central through the
Central Portal.

## Current Release Scope

The first public release was `0.1.0` and included:

- `io.github.qbsstg:protocol-sdk`
- `io.github.qbsstg:protocol-core`
- `io.github.qbsstg:protocol-iec104`

The parent POM is for building the SDK workspace. Applications should depend on
protocol modules directly.

The latest published release is `0.5.0`; its readiness decision is documented
in [`release-readiness-0.5.0.md`](release-readiness-0.5.0.md). The selected
`0.5.0` policy was to publish the current Maven reactor as one versioned
release, including experimental `protocol-modbus`, while release notes and
module docs keep Modbus clearly marked as experimental and non-blocking.

The `0.5.0` release notes are maintained in
[`release-notes-0.5.0.md`](release-notes-0.5.0.md).

The next SDK target is `0.6.0`, focused on promoting `protocol-modbus` from an
experimental TCP/UDP parser baseline to a stable parser module. The plan is
tracked in [`release-plan-0.6.0.md`](release-plan-0.6.0.md).

## Prerequisites

1. Verify the `io.github.qbsstg` namespace in the Central Portal.
2. Generate a Central Portal user token.
3. Create or choose a GPG key for artifact signing.
4. Publish the public GPG key to a supported key server.
5. Keep Central credentials and GPG secrets outside this repository.

The Maven server id used by this project is `central`.

Example `~/.m2/settings.xml` server entry:

```xml
<settings>
  <servers>
    <server>
      <id>central</id>
      <username>${env.CENTRAL_TOKEN_USERNAME}</username>
      <password>${env.CENTRAL_TOKEN_PASSWORD}</password>
    </server>
  </servers>
</settings>
```

## Local Checks

Run the normal verification before release work:

```bash
mvn -q verify
```

The build should generate these artifacts for each jar module:

- main jar
- sources jar
- Javadoc jar

To verify the release profile wiring without requiring GPG signing or Central
credentials, run:

```bash
mvn -q -Pcentral-release \
  -Dgpg.skip=true \
  -Dcentral.skipPublishing=true \
  deploy
```

This is only a profile smoke check. It must not replace the signed dry run
before a real release.

## Signed Dry Run

Use the `central-release` profile to sign artifacts. The following command keeps
publishing disabled, so it can be used to verify signing and Central bundle
generation without uploading a deployment:

```bash
mvn -Pcentral-release -Dcentral.skipPublishing=true clean deploy
```

This command requires local GPG signing to work. Prefer `gpg-agent` for
passphrase handling so secrets do not enter shell history or project files.

## Manual Publishing Flow

The default release profile uses manual Central Portal publishing:

```bash
mvn -Pcentral-release clean deploy
```

By default:

- `autoPublish` is `false`
- the deployment is uploaded and validated
- final publishing is completed manually in the Central Portal

This is intentional for early releases because it leaves a review point before
artifacts become immutable on Maven Central.

## Automatic Publishing

After the manual process has been proven, automatic publishing can be enabled
per run:

```bash
mvn -Pcentral-release \
  -Dcentral.autoPublish=true \
  clean deploy
```

Do not enable automatic publishing in CI until signing keys, Central token
scope, tag policy, and rollback expectations are settled.

## Release Checklist

1. Confirm `main` is clean and CI is green.
2. Confirm the release-readiness note for the target version is current.
3. Update versions from the current `-SNAPSHOT` version to the release version.
4. Confirm README and module docs label experimental modules accurately.
5. Run `mvn -q verify`.
6. Run the signed dry run.
7. Tag the release commit.
8. Run the manual publishing command.
9. Review and publish the validated deployment in the Central Portal.
10. Publish the GitHub release notes from the target release-note draft.
11. Move versions to the next `-SNAPSHOT`.

## References

- Central Portal Maven publishing:
  https://central.sonatype.org/publish/publish-portal-maven/
- Central Portal token generation:
  https://central.sonatype.org/publish/generate-portal-token/
- Central GPG requirements:
  https://central.sonatype.org/publish/requirements/gpg/
