package com.blaj.openmetin.contracts.annotation;

import com.blaj.openmetin.contracts.enums.PacketDirection;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PacketHeader {
  int header();

  PacketDirection[] direction();

  boolean isSequence() default false;

  boolean hasDynamicSize() default false;
}
