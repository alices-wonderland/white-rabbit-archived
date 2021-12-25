package com.ukonnra.wonderland.whiterabbit.core.entity;

import com.querydsl.core.types.dsl.BooleanExpression;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.lang.Nullable;

@Entity(name = "books")
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public final class Book extends AbstractEntity {
  private String name;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @ToString.Exclude
  private User author;

  public static BooleanExpression comparableFieldHandler(
      String name, @Nullable Boolean isAfter, Object value) {
    if (name.equals("id")) {
      return doCompare(
          isAfter,
          QBook.book.id::gt,
          QBook.book.id::lt,
          QBook.book.id::eq,
          UUID.fromString(value.toString()));
    } else if (name.equals("name")) {
      return doCompare(
          isAfter, QBook.book.name::gt, QBook.book.name::lt, QBook.book.name::eq, value.toString());
    } else if (name.equals("version") && value instanceof Number item) {
      return doCompare(
          isAfter,
          QBook.book.version::gt,
          QBook.book.version::lt,
          QBook.book.version::eq,
          item.longValue());
    } else {
      throw new RuntimeException(String.format("Field[%s] is not sortable", name));
    }
  }
}
