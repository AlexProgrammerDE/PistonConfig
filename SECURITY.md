# Security Policy

## Supported Versions

PistonConfig is pre-1.0. Until the first stable release, security fixes target the latest `main` branch and the newest published release line.

## Reporting a Vulnerability

Please do not open a public issue for security reports.

Use GitHub private vulnerability reporting for `AlexProgrammerDE/PistonConfig` when available. If that is not available, contact the maintainer through the GitHub profile at <https://github.com/AlexProgrammerDE>.

Include:

- the affected module and version
- a minimal reproduction or malformed config file
- expected impact
- whether the issue is already public

## Scope

Security-sensitive areas include parser backend behavior, unsafe type conversion, environment overrides, file loading and saving, migration execution, and any behavior that could cause unexpected code execution, data exposure, or config corruption.
