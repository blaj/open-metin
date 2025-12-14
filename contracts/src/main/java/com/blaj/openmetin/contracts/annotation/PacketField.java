package com.blaj.openmetin.contracts.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PacketField {
  int position();

  boolean unsigned() default false;

  int length() default -1;

  int arrayLength() default -1;
}
