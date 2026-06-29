# Contributing

Thanks for working on PistonConfig. Keep changes focused, typed, and easy to review.

## Development

Use Java 25 and the checked-in Gradle Wrapper.

```bash
./gradlew test
./gradlew build
```

## Code Style

- Prefer records, sealed interfaces, enhanced switches, pattern matching, and strong generics.
- Use Immutables staged builders for public immutable API objects when construction order matters.
- Lombok is available, but use it only when it clearly reduces internal boilerplate.
- Do not put backend-specific parser objects in public APIs. Translate parser details into core comments, decorations, metadata keys, and source locations.
- Add constants for metadata keys instead of repeating raw strings.

## Pull Requests

- Keep commits in Conventional Commit format, such as `fix: preserve yaml key comments`.
- Add targeted tests for behavior changes.
- Run `./gradlew test` before opening a PR.
- Do not bypass Git hooks.

## Publishing

Release artifacts are staged with Gradle and deployed through JReleaser. Releases require signing and Maven Central credentials configured as GitHub Actions secrets.
