package com.ukonnra.wonderland.whiterabbit.core;

import com.ukonnra.wonderland.whiterabbit.core.entity.User;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.lang.Nullable;

public abstract class CoreException extends RuntimeException {
  protected CoreException(String message) {
    super(message);
  }

  private static String extractType(final Class<?> clazz) {
    return clazz.getSimpleName();
  }

  @Getter
  @EqualsAndHashCode(callSuper = false)
  public static final class NotFound extends CoreException {
    private final String type;
    private final UUID id;

    public NotFound(Class<?> clazz, UUID id) {
      super(
          String.format("Type[%s] with ID[%s] is not found", CoreException.extractType(clazz), id));
      this.type = CoreException.extractType(clazz);
      this.id = id;
    }
  }

  @Getter
  @EqualsAndHashCode(callSuper = false)
  public static final class FieldNotSortable extends CoreException {
    private final String type;
    private final String field;

    public FieldNotSortable(Class<?> clazz, String field) {
      super(
          String.format(
              "Field[%s] for Type[%s] is not sortable", field, CoreException.extractType(clazz)));
      this.type = CoreException.extractType(clazz);
      this.field = field;
    }
  }

  @Getter
  @EqualsAndHashCode(callSuper = false)
  public static final class AccessDeniedOnEntityField extends CoreException {
    @Nullable private final UUID id;
    private final String type;
    private final String field;

    public AccessDeniedOnEntityField(User user, Class<?> clazz, String field) {
      super(
          String.format(
              "User[%s] cannot access Field[%s] for Type[%s]",
              user.getId(), field, CoreException.extractType(clazz)));
      this.id = user.getId();
      this.type = CoreException.extractType(clazz);
      this.field = field;
    }
  }
}
