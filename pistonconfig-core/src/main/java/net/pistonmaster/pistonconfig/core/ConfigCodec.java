package net.pistonmaster.pistonconfig.core;

/**
 * Converts between Java values and configuration nodes.
 *
 * @param <T> Java type handled by this codec
 */
public interface ConfigCodec<T> {
  ConfigNode encode(T value, ConfigCodecRegistry registry);

  T decode(ConfigNode node, ConfigCodecRegistry registry);
}
