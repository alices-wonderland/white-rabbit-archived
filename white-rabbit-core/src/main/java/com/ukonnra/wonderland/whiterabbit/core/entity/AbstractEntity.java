package com.ukonnra.wonderland.whiterabbit.core.entity;

import com.querydsl.core.types.dsl.BooleanExpression;
import java.util.UUID;
import java.util.function.Function;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.jpa.domain.AbstractAuditable;
import org.springframework.lang.Nullable;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@MappedSuperclass
public abstract class AbstractEntity extends AbstractAuditable<User, UUID> {
  @Version private Long version;

  protected static <T extends Comparable<T>> BooleanExpression doCompare(
      @Nullable Boolean isAfter,
      Function<T, BooleanExpression> left,
      Function<T, BooleanExpression> right,
      Function<T, BooleanExpression> eq,
      T value) {
    if (isAfter == null) {
      return eq.apply(value);
    } else if (isAfter) {
      return left.apply(value);
    } else {
      return right.apply(value);
    }
  }
}
