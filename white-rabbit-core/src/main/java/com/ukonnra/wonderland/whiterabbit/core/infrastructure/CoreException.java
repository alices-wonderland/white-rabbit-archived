package com.ukonnra.wonderland.whiterabbit.core.infrastructure;

import com.ukonnra.wonderland.whiterabbit.core.domain.user.User;
import java.util.Optional;
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

    public NotFound(AbstractEntity<?> entity) {
      this(entity.getClass(), entity.getId());
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

    public AccessDeniedOnEntityField(User operator, AbstractEntity<?> entity, String field) {
      this(operator.getId(), CoreException.extractType(entity.getClass()), entity.getId(), field);
    }
  }

  @Getter
  @EqualsAndHashCode(callSuper = false)
  public static final class AlreadyInitialized extends CoreException {
    @Nullable private final UUID id;
    private final String type;

    public AlreadyInitialized(AbstractEntity<?> entity) {
      super(
          String.format(
              "Type[%s, id=%s] is already initialized, cannot be created again",
              CoreException.extractType(entity.getClass()), entity.getId()));
      this.id = entity.getId();
      this.type = CoreException.extractType(entity.getClass());
    }
  }

  @Getter
  @EqualsAndHashCode(callSuper = false)
  public static final class OperatorNotWriteable extends CoreException {
    @Nullable private final UUID operatorId;
    private final String type;
    private final UUID id;

    public OperatorNotWriteable(final @Nullable UUID operatorId, final String type, final UUID id) {
      super(String.format("Operator[%s] cannot write/edit Type[%s, id=%s]", operatorId, type, id));

      this.operatorId = operatorId;
      this.type = type;
      this.id = id;
    }

    public OperatorNotWriteable(@Nullable final User operator, final AbstractEntity<?> entity) {
      this(
          Optional.ofNullable(operator).map(User::getId).orElse(null),
          CoreException.extractType(entity.getClass()),
          entity.getId());
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

    public UserOperatorCannotCreateUser(final User operator) {
      super(String.format("Operator[%s, role=USER] cannot create users", operator.getId()));
      this.operatorId = operator.getId();
    }
  }

  @Getter
  @EqualsAndHashCode(callSuper = false)
  public static final class AdminOperatorCannotCreateUserRoleNotEqualsUser extends CoreException {
    private final UUID operatorId;

    public AdminOperatorCannotCreateUserRoleNotEqualsUser(final User operator) {
      super(
          String.format(
              "Operator[%s, role=ADMIN] cannot create users whose role is not USER",
              operator.getId()));
      this.operatorId = operator.getId();
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

    public EmptyUpdateOperation(final AbstractEntity<?> entity) {
      this(CoreException.extractType(entity.getClass()), entity.getId());
    }
  }

  @Getter
  @EqualsAndHashCode(callSuper = false)
  public static final class FieldCouldNotBeEmpty extends CoreException {
    private final String type;
    private final String field;

    private FieldCouldNotBeEmpty(final String type, final String field) {
      super(String.format("Field[%s] of Type[%s] could not be empty", field, type));
      this.type = type;
      this.field = field;
    }

    public FieldCouldNotBeEmpty(
        final Class<? extends AbstractEntity<?>> clazz, final String field) {
      this(CoreException.extractType(clazz), field);
    }
  }

  @Getter
  @EqualsAndHashCode(callSuper = false)
  public static final class NestedEntityNotMatch extends CoreException {
    private final String parentType;
    private final UUID parentId;
    private final String childType;
    private final UUID childId;

    private NestedEntityNotMatch(
        final String parentType, final UUID parentId, final String childType, final UUID childId) {
      super(
          String.format(
              "The parent of child Type[%s, id=%s] does not match Type[%s, id=%s]",
              childType, childId, parentType, parentId));
      this.parentType = parentType;
      this.parentId = parentId;
      this.childType = childType;
      this.childId = childId;
    }

    public NestedEntityNotMatch(final AbstractEntity<?> parent, final AbstractEntity<?> child) {
      this(
          CoreException.extractType(parent.getClass()),
          parent.getId(),
          CoreException.extractType(child.getClass()),
          child.getId());
    }
  }

  @Getter
  @EqualsAndHashCode(callSuper = false)
  public static final class InventoryFifoCannotContainMultipleUnitRecords extends CoreException {
    private final UUID id;

    public InventoryFifoCannotContainMultipleUnitRecords(final UUID id) {
      super(
          String.format(
              "Inventory[%s] contains records with multiple units, which is invalid", id));
      this.id = id;
    }
  }

  @Getter
  @EqualsAndHashCode(callSuper = false)
  public static final class InventoryFifoCannotMixRecordsWithAndWithoutPrices
      extends CoreException {
    private final UUID id;

    public InventoryFifoCannotMixRecordsWithAndWithoutPrices(final UUID id) {
      super(
          String.format(
              "Inventory[%s] contains mixes records with and without prices, which is invalid",
              id));
      this.id = id;
    }
  }
}
