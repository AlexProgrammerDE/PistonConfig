package net.pistonmaster.pistonconfig.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

final class SimpleAnnotatedType implements AnnotatedType {
  private final Type type;
  private final Annotation[] annotations;

  SimpleAnnotatedType(Type type) {
    this(type, new Annotation[0]);
  }

  SimpleAnnotatedType(Type type, Annotation[] annotations) {
    this.type = Objects.requireNonNull(type, "type");
    this.annotations = annotations == null ? new Annotation[0] : annotations.clone();
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
    return Arrays.stream(annotations)
      .filter(annotationClass::isInstance)
      .map(annotationClass::cast)
      .findFirst()
      .orElse(null);
  }

  @Override
  public Annotation[] getAnnotations() {
    return annotations.clone();
  }

  @Override
  public Annotation[] getDeclaredAnnotations() {
    return getAnnotations();
  }
}
