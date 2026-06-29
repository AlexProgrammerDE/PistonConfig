package net.pistonmaster.pistonconfig.core;

import org.immutables.value.Value;

/// Shared Immutables style for core API value objects.
@Value.Style(
  stagedBuilder = true,
  strictBuilder = true,
  typeImmutable = "Immutable*",
  jdkOnly = true
)
public @interface PistonStyle {
}
