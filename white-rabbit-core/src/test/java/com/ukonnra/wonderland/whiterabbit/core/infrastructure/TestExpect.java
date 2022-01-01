package com.ukonnra.wonderland.whiterabbit.core.infrastructure;

import java.util.function.Consumer;
import java.util.function.Supplier;

public sealed interface TestExpect<C> {
  Supplier<C> command();

  record Success<C, R>(Supplier<C> command, Consumer<R> handler) implements TestExpect<C> {
    public Success(C command, Consumer<R> handler) {
      this(() -> command, handler);
    }
  }

  record Failure<C, ExT extends CoreException>(Supplier<C> command, Class<ExT> exceptionClass)
      implements TestExpect<C> {
    public Failure(C command, Class<ExT> exceptionClass) {
      this(() -> command, exceptionClass);
    }
  }
}
