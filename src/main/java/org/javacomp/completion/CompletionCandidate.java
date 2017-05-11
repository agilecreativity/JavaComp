package org.javacomp.completion;

import java.util.Optional;

public interface CompletionCandidate {
  public enum Kind {
    UNKNOWN,
    CLASS,
    INTERFACE,
    ENUM,
    METHOD,
    VARIABLE,
    FIELD,
    PACKAGE,
    KEYWORD,
  }

  public abstract String getName();

  public abstract Kind getKind();

  public abstract Optional<String> getDetail();
}
