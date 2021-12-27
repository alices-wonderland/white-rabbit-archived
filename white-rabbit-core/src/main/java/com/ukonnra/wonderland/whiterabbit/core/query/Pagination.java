package com.ukonnra.wonderland.whiterabbit.core.query;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import javax.validation.constraints.Positive;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;

public sealed interface Pagination permits Pagination.Bidirectional, Pagination.Unidirectional {
  @Positive
  Integer size();

  boolean isAfter();

  /**
   * Extract ID from Cursor, right now Cursor is <code>base64(id)</code>
   *
   * @param cursor The bast64 cursor string
   * @return Entity ID
   */
  private static UUID extractIdFromCursor(final String cursor) {
    return UUID.fromString(
        new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8));
  }

  private static BooleanExpression createPaginationFilter(
      final String cursor,
      final ComparableFieldHandler handler,
      final Sort.Order order,
      @Nullable Boolean isAfter) {
    var id = extractIdFromCursor(cursor);
    if (isAfter == null) {
      return handler.handle(id, order.getProperty(), null);
    } else {
      return handler.handle(
          id,
          order.getProperty(),
          (isAfter && order.isAscending()) || (!isAfter && order.isDescending()));
    }
  }

  static BooleanExpression createPaginationFilter(
      final String cursor, final ComparableFieldHandler handler, final Sort sort, boolean isAfter) {
    var id = extractIdFromCursor(cursor);

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

  default Sort createSort(final Sort sort) {
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
  interface ComparableFieldHandler {
    BooleanExpression handle(UUID id, String field, @Nullable Boolean isAfter);
  }

  BooleanExpression createFilter(final ComparableFieldHandler handler, final Sort sort);

  record Unidirectional(@Nullable String cursor, boolean isAfter, @Positive Integer size)
      implements Pagination {

    @Override
    public @Nullable BooleanExpression createFilter(ComparableFieldHandler handler, Sort sort) {
      return cursor == null ? null : createPaginationFilter(cursor, handler, sort, isAfter);
    }
  }

  record Bidirectional(
      String after, String before, @Nullable @Positive Integer size, boolean isAfter)
      implements Pagination {
    @Override
    public BooleanExpression createFilter(ComparableFieldHandler handler, Sort sort) {
      return Expressions.allOf(
          createPaginationFilter(after, handler, sort, true),
          createPaginationFilter(before, handler, sort, false));
    }
  }
}
