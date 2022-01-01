package com.ukonnra.wonderland.whiterabbit.core.infrastructure;

import com.ukonnra.wonderland.whiterabbit.core.domain.user.User;
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
    @Nullable private final UUID operatorId;
    private final String type;
    private final UUID id;
    private final String field;

    public AccessDeniedOnEntityField(
        @Nullable UUID operatorId, String type, UUID id, String field) {
      super(
          String.format(
              "User[%s] cannot access Field[%s] for Type[%s, id=%s]", operatorId, field, type, id));
      this.operatorId = operatorId;
      this.type = type;
      this.id = id;
      this.field = field;
    }

    public AccessDeniedOnEntityField(
        User.PresentationModel operator, AbstractAggregateRoot<?, ?> aggregateRoot, String field) {
      this(
          operator.id(),
          CoreException.extractType(aggregateRoot.getClass()),
          aggregateRoot.getId(),
          field);
    }
  }

  @Getter
  @EqualsAndHashCode(callSuper = false)
  public static final class AlreadyInitialized extends CoreException {
    @Nullable private final UUID id;
    private final String type;

    public AlreadyInitialized(AbstractAggregateRoot<?, ?> aggregateRoot) {
      super(
          String.format(
              "Type[%s, id=%s] is already initialized, cannot be created again",
              CoreException.extractType(aggregateRoot.getClass()), aggregateRoot.getId()));
      this.id = aggregateRoot.getId();
      this.type = CoreException.extractType(aggregateRoot.getClass());
    }
  }

  @Getter
  @EqualsAndHashCode(callSuper = false)
  public static final class OperatorNotWriteable extends CoreException {
    private final UUID operatorId;
    private final String type;
    private final UUID id;

    public OperatorNotWriteable(
        final User.PresentationModel operator, final AbstractAggregateRoot<?, ?> aggregateRoot) {
      super(
          String.format(
              "Operator[%s] cannot write/edit Type[%s, id=%s]",
              operator.id(),
              CoreException.extractType(aggregateRoot.getClass()),
              aggregateRoot.getId()));
      this.operatorId = operator.id();
      this.id = aggregateRoot.getId();
      this.type = CoreException.extractType(aggregateRoot.getClass());
    }
  }

  @EqualsAndHashCode(callSuper = false)
  public static final class InvalidToken extends CoreException {
    public InvalidToken() {
      super("The authentication token is invalid");
    }
  }

  @EqualsAndHashCode(callSuper = false)
  public static final class NullOperatorCannotCreateUserRoleNotEqualsUser extends CoreException {
    public NullOperatorCannotCreateUserRoleNotEqualsUser() {
      super("Operator[null] cannot create users with role != Role.USER");
    }
  }

  @EqualsAndHashCode(callSuper = false)
  public static final class NullOperatorCannotCreateUserNotSingleIdentifier extends CoreException {
    public NullOperatorCannotCreateUserNotSingleIdentifier() {
      super("Operator[null] cannot create users without a single identifier");
    }
  }

  @EqualsAndHashCode(callSuper = false)
  public static final class NullOperatorCannotCreateUserIdentifierNotMatch extends CoreException {
    public NullOperatorCannotCreateUserIdentifierNotMatch() {
      super(
          "Operator[null] cannot create users whose identifier does not match the authentication token");
    }
  }

  @Getter
  @EqualsAndHashCode(callSuper = false)
  public static final class UserOperatorCannotCreateUser extends CoreException {
    private final UUID operatorId;

    public UserOperatorCannotCreateUser(final User.PresentationModel operator) {
      super(String.format("Operator[%s, role=USER] cannot create users", operator.id()));
      this.operatorId = operator.id();
    }
  }

  @Getter
  @EqualsAndHashCode(callSuper = false)
  public static final class AdminOperatorCannotCreateUserRoleNotEqualsUser extends CoreException {
    private final UUID operatorId;

    public AdminOperatorCannotCreateUserRoleNotEqualsUser(final User.PresentationModel operator) {
      super(
          String.format(
              "Operator[%s, role=ADMIN] cannot create users whose role is not USER",
              operator.id()));
      this.operatorId = operator.id();
    }
  }

  @EqualsAndHashCode(callSuper = false)
  public static final class UserIdentifierCannotEmpty extends CoreException {
    public UserIdentifierCannotEmpty() {
      super("Field[identifier] for Type[User] cannot be empty");
    }
  }

  @Getter
  @EqualsAndHashCode(callSuper = false)
  public static final class EmptyUpdateOperation extends CoreException {
    private final String type;
    private final UUID id;

    private EmptyUpdateOperation(final String type, final UUID id) {
      super(String.format("Type[%s, id=%s] cannot be updated with empty operation", type, id));
      this.type = type;
      this.id = id;
    }

    public EmptyUpdateOperation(final AbstractAggregateRoot<?, ?> aggregateRoot) {
      this(CoreException.extractType(aggregateRoot.getClass()), aggregateRoot.getId());
    }
  }
}
