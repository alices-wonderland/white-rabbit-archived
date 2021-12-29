package com.ukonnra.wonderland.whiterabbit.endpoint.graphql;

import com.ukonnra.wonderland.whiterabbit.core.infrastructure.CoreException;
import lombok.EqualsAndHashCode;

public abstract sealed class ApplicationException extends CoreException {
  protected ApplicationException(String message) {
    super(message);
  }

  @EqualsAndHashCode(callSuper = false)
  public static final class ExclusiveFirstAndLastFields extends ApplicationException {
    public ExclusiveFirstAndLastFields() {
      super(
          "Field[first] and Field[last] are exclusive when querying, please use at most one in each query");
    }
  }
}
