package com.ukonnra.wonderland.whiterabbit.core.entity;

import com.querydsl.core.types.dsl.BooleanExpression;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.lang.Nullable;

@Entity(name = "users")
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public final class User extends AbstractEntity {
  @NotBlank private String name;

  @ManyToOne @Nullable private User manager;

  @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  private Set<Book> books = new HashSet<>();

  public static BooleanExpression comparableFieldHandler(
      String name, @Nullable Boolean isAfter, Object value) {
    if (name.equals("id")) {
      return doCompare(
          isAfter,
          QUser.user.id::gt,
          QUser.user.id::lt,
          QUser.user.id::eq,
          UUID.fromString(value.toString()));
    } else if (name.equals("name")) {
      return doCompare(
          isAfter, QUser.user.name::gt, QUser.user.name::lt, QUser.user.name::eq, value.toString());
    } else if (name.equals("version") && value instanceof Number item) {
      return doCompare(
          isAfter,
          QUser.user.version::gt,
          QUser.user.version::lt,
          QUser.user.version::eq,
          item.longValue());
    } else {
      throw new RuntimeException(String.format("Field[%s] is not sortable", name));
    }
  }
}
