package net.pistonmaster.pistonconfig.core;

/**
 * Controls how list nodes behave when a current document is merged with defaults.
 */
public enum MergeListStrategy {
  PRESERVE_EXISTING,
  REPLACE,
  APPEND_MISSING
}
