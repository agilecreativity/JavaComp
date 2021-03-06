package org.javacomp.model;

import com.google.auto.value.AutoValue;

/** A solved type that is an array. */
@AutoValue
public abstract class SolvedArrayType implements SolvedType {
  public abstract SolvedType getBaseType();

  public static SolvedArrayType create(SolvedType baseType) {
    return new AutoValue_SolvedArrayType(baseType);
  }
}
