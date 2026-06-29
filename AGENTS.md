# Agent Instructions

- Use Conventional Commit messages.
- Do not bypass Git hooks.
- Do not create branches unless explicitly asked.
- Target Java 25.
- Prefer records, sealed interfaces, enhanced switches, pattern matching, and strong generics.
- Prefer Immutables staged builders for public immutable API objects where builders improve type safety.
- Lombok is available through the FreeFair plugin, but use it only when it reduces internal boilerplate without weakening the public API.
- Keep backend-specific parser details represented by core-owned metadata and decorations where practical.
- Do not use magic metadata strings directly. Add constants classes for public keys.
