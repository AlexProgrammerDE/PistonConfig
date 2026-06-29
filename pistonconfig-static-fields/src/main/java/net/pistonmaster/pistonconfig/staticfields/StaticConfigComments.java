package net.pistonmaster.pistonconfig.staticfields;

/// Implemented by static config holder classes that register section, root, or
/// footer comments separately from individual properties.
public interface StaticConfigComments {
  /// Registers generated comments.
  ///
  /// @param comments comment registry
  void registerComments(StaticConfigCommentRegistry comments);
}
