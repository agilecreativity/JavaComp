package org.javacomp.model;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;
import java.util.List;
import java.util.Optional;
import org.javacomp.model.util.QualifiedNames;

/** Represents a method. */
public class MethodEntity extends Entity implements EntityScope {
  private static final String CONSTRUCTOR_NAME = "<init>";

  private final TypeReference returnType;
  private final ImmutableList<VariableEntity> parameters;
  private final ImmutableList<TypeParameter> typeParameters;
  // Map of simple names -> entities.
  private final Multimap<String, Entity> entities;
  private final ClassEntity classEntity;

  public MethodEntity(
      String simpleName,
      List<String> qualifiers,
      boolean isStatic,
      TypeReference returnType,
      List<VariableEntity> parameters,
      List<TypeParameter> typeParameters,
      ClassEntity classEntity,
      Range<Integer> methodNamelRange) {
    super(simpleName, Entity.Kind.METHOD, qualifiers, isStatic, methodNamelRange);
    this.returnType = returnType;
    this.parameters = ImmutableList.copyOf(parameters);
    this.typeParameters = ImmutableList.copyOf(typeParameters);
    this.entities = HashMultimap.create();
    this.classEntity = classEntity;
  }

  /////////////// Entity methods ////////////////

  @Override
  public MethodEntity getChildScope() {
    return this;
  }

  /////////////// EntityScope methods ///////////////

  @Override
  public Multimap<String, Entity> getMemberEntities() {
    ImmutableMultimap.Builder<String, Entity> builder = new ImmutableMultimap.Builder<>();
    builder.putAll(entities);
    for (VariableEntity parameter : parameters) {
      builder.put(parameter.getSimpleName(), parameter);
    }
    return builder.build();
  }

  @Override
  public void addEntity(Entity entity) {
    entities.put(entity.getSimpleName(), entity);
  }

  @Override
  public Optional<EntityScope> getParentScope() {
    return Optional.of(classEntity);
  }

  /////////////// Other methods ////////////////

  public ImmutableList<VariableEntity> getParameters() {
    return parameters;
  }

  public ImmutableList<TypeParameter> getTypeParameters() {
    return typeParameters;
  }

  public TypeReference getReturnType() {
    return returnType;
  }

  public ClassEntity getParentClass() {
    return classEntity;
  }

  public boolean isConstructor() {
    return CONSTRUCTOR_NAME.equals(getSimpleName());
  }

  @Override
  public String toString() {
    return "MethodEntity<<"
        + getTypeParameters()
        + "> "
        + QualifiedNames.formatQualifiedName(getQualifiers(), getSimpleName())
        + ">";
  }
}
