package org.javacomp.completion.testdata;

public class CompleteNewStatement {
  private static final String CONSTANT = "constant";
  private final int memberField = 1;

  public CompleteNewStatement() {
  }

  public void staticMethod() {
  }

  public void memberMethod(boolean param1, String stringParam) {
    /**@complete*/
  }
}
