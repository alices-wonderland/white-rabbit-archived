package com.ukonnra.wonderland.whiterabbit.core.domain.user;

import com.ukonnra.wonderland.whiterabbit.core.domain.user.valobj.Identifier;
import com.ukonnra.wonderland.whiterabbit.core.infrastructure.AbstractAggregateRoot;
import com.ukonnra.wonderland.whiterabbit.core.infrastructure.AbstractPresentationModel;
import com.ukonnra.wonderland.whiterabbit.core.infrastructure.CoreException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.lang.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Entity(name = "users")
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Slf4j
@SuppressWarnings("squid:S2160")
public final class User extends AbstractAggregateRoot<User.PresentationModel, UserEvent> {
  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private Role role;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(uniqueConstraints = @UniqueConstraint(columnNames = {"type", "value"}))
  private Set<Identifier> identifiers = new HashSet<>();

  @Override
  public PresentationModel toPresentationModel() {
    return new PresentationModel(this.getId(), this.name, this.role, this.identifiers);
  }

  public enum Role {
    OWNER(null),
    ADMIN(Role.OWNER),
    USER(Role.ADMIN);

    public final GrantedAuthority grantedAuthority;

    @Nullable public final Role parent;

    Role(@Nullable final Role parent) {
      this.grantedAuthority = new SimpleGrantedAuthority("ROLE_" + this.name());
      this.parent = parent;
    }
  }

  public record PresentationModel(UUID id, String name, Role role, Set<Identifier> identifiers)
      implements AbstractPresentationModel, UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
      var roles =
          switch (this.role) {
            case OWNER -> Set.of(Role.values());
            case ADMIN -> Set.of(Role.ADMIN, Role.USER);
            case USER -> Set.of(Role.USER);
          };

      return roles.stream().map(r -> r.grantedAuthority).collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
      return null;
    }

    @Override
    public String getUsername() {
      return Optional.ofNullable(this.id).map(UUID::toString).orElse(null);
    }

    @Override
    public boolean isAccountNonExpired() {
      return false;
    }

    @Override
    public boolean isAccountNonLocked() {
      return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
      return false;
    }

    @Override
    public boolean isEnabled() {
      return true;
    }
  }

  /**
   *
   *
   * <ul>
   *   <li>If {@code operator.role == Role.USER}
   *       <ul>
   *         <li>Can only update self
   *       </ul>
   *   <li>If {@code Role.ADMIN}
   *       <ul>
   *         <li>Can update self or any users {@code role == Role.USER}
   *       </ul>
   *   <li>If {@code Role.OWNER}
   *       <ul>
   *         <li>Can update any users
   *       </ul>
   * </ul>
   *
   * @param operator
   */
  public boolean isWriteable(@Nullable final User.PresentationModel operator) {
    if (operator == null) {
      return false;
    } else if (operator.role == Role.OWNER) {
      return true;
    } else if (operator.role == Role.ADMIN) {
      return this.getId() != null
          && (Objects.equals(operator.id, this.getId()) || this.role == Role.USER);
    } else {
      return this.getId() != null && Objects.equals(operator.id, this.getId());
    }
  }

  /**
   *
   *
   * <ul>
   *   <li>If {@code operator == null}
   *       <ul>
   *         <li>Can only create users with {@code role = Role.USER & identifier = {provider,
   *             provider-user-id}}
   *       </ul>
   *   <li>If {@code operator.role == Role.USER}
   *       <ul>
   *         <li>Cannot create users
   *       </ul>
   *   <li>If {@code Role.ADMIN}
   *       <ul>
   *         <li>Can create users with {@code Role.USER}
   *       </ul>
   *   <li>If {@code Role.OWNER}
   *       <ul>
   *         <li>Can create users with any role
   *       </ul>
   * </ul>
   *
   * @param operator
   */
  public void create(
      @Nullable final User.PresentationModel operator,
      JwtAuthenticationToken jwt,
      final String name,
      final Role role,
      final Set<Identifier> identifiers) {
    if (this.getId() != null) {
      throw new CoreException.AlreadyInitialized(this);
    }

    if (operator == null) {
      if (role != null && role != Role.USER) {
        throw new CoreException.NullOperatorCannotCreateUserRoleNotEqualsUser();
      }
      if (identifiers.size() != 1) {
        throw new CoreException.NullOperatorCannotCreateUserNotSingleIdentifier();
      }

      if (!identifiers.contains(new Identifier(jwt.getToken()))) {
        throw new CoreException.NullOperatorCannotCreateUserIdentifierNotMatch();
      }
    } else if (operator.role == Role.USER) {
      throw new CoreException.UserOperatorCannotCreateUser(operator);
    } else if (operator.role == Role.ADMIN && role != Role.USER) {
      throw new CoreException.AdminOperatorCannotCreateUserRoleNotEqualsUser(operator);
    }

    if (identifiers.isEmpty()) {
      throw new CoreException.UserIdentifierCannotEmpty();
    }

    this.name = name;
    this.role = role;
    this.identifiers = identifiers;
  }

  /**
   *
   *
   * <ul>
   *   <li>If {@code operator.role == Role.USER | Role.ADMIN}
   *       <ul>
   *         <li>Can only update field {@code name} and {@code identifier}
   *       </ul>
   *   <li>If {@code Role.OWNER}
   *       <ul>
   *         <li>Can update all fields
   *       </ul>
   * </ul>
   *
   * @param operator
   * @param name
   * @param role
   */
  public void update(
      final User.PresentationModel operator,
      @Nullable final String name,
      @Nullable final Role role,
      @Nullable final Set<Identifier> identifiers) {
    if (name == null && role == null && identifiers == null) {
      throw new CoreException.EmptyUpdateOperation(this);
    }

    if (!this.isWriteable(operator)) {
      throw new CoreException.OperatorNotWriteable(operator, this);
    }

    if (name != null) {
      this.name = name;
    }

    if (role != null) {
      if (operator.role != Role.OWNER) {
        throw new CoreException.AccessDeniedOnEntityField(operator, this, "role");
      }

      this.role = role;
    }

    if (identifiers != null) {
      if (identifiers.isEmpty()) {
        throw new CoreException.UserIdentifierCannotEmpty();
      }
      this.identifiers = identifiers;
    }
  }
}
