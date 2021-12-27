package com.ukonnra.wonderland.whiterabbit.core.query;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import java.util.UUID;
import javax.validation.constraints.Positive;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;

public record Pagination(
    @Nullable String after, @Nullable String before, @Positive int size, boolean isAfter) {
  private static BooleanExpression createPaginationFilter(
      final String cursor,
      final ComparableFieldHandler handler,
      final Sort.Order order,
      @Nullable Boolean isAfter) {
    var id = Cursor.extractId(cursor);
    if (isAfter == null) {
      return handler.handle(id, order.getProperty(), null);
    } else {
      return handler.handle(
          id,
          order.getProperty(),
          (isAfter && order.isAscending()) || (!isAfter && order.isDescending()));
    }
  }

  private static BooleanExpression createPaginationFilter(
      final String cursor, final ComparableFieldHandler handler, final Sort sort, boolean isAfter) {
    var id = Cursor.extractId(cursor);

    var whenNotMatching =
        Expressions.allOf(
            sort.stream()
                .map(o -> createPaginationFilter(cursor, handler, o, isAfter))
                .toArray(BooleanExpression[]::new));
    var whenMatching =
        Expressions.allOf(
                sort.stream()
                    .map(o -> createPaginationFilter(cursor, handler, o, null))
                    .toArray(BooleanExpression[]::new))
            .and(handler.handle(id, "id", isAfter));
    return whenMatching.or(whenNotMatching);
  }

  public Sort createSort(final Sort sort) {
    var orders =
        sort.stream()
            .map(
                item -> {
                  var direction = item.getDirection();
                  if (!this.isAfter()) {
                    direction = direction.isAscending() ? Sort.Direction.DESC : Sort.Direction.ASC;
                  }
                  return new Sort.Order(direction, item.getProperty(), item.getNullHandling());
                })
            .toList();
    return Sort.by(orders);
  }

  @FunctionalInterface
  public interface ComparableFieldHandler {
    BooleanExpression handle(UUID id, String field, @Nullable Boolean isAfter);
  }

  public BooleanExpression createFilter(ComparableFieldHandler handler, Sort sort) {
    var afterFilter = after == null ? null : createPaginationFilter(after, handler, sort, true);
    var beforeFilter = before == null ? null : createPaginationFilter(before, handler, sort, false);
    if (afterFilter != null && beforeFilter != null) {
      return afterFilter.and(beforeFilter);
    } else {
      return afterFilter != null ? afterFilter : beforeFilter;
    }
  }
}
