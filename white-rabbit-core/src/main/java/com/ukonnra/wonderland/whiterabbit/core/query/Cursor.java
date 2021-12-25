package com.ukonnra.wonderland.whiterabbit.core.query;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.validation.constraints.Positive;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;

public record Cursor(UUID id, Map<String, Object> values) {
  public Cursor(UUID id, Map<String, Object> values) {
    this.id = id;
    this.values =
        values.entrySet().stream()
            .filter(e -> !e.getKey().equals("id"))
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @FunctionalInterface
  public interface ComparableFieldHandler {
    BooleanExpression handle(String name, @Nullable Boolean isAfter, Object value);
  }

  public sealed interface Pagination permits Pagination.Bidirectional, Pagination.Unidirectional {
    @Nullable
    @Positive
    Integer getSize();

    boolean isAfter();

    static BooleanExpression createAdditionalFilter(
        final Cursor cursor,
        final ComparableFieldHandler handler,
        final Sort.Order order,
        @Nullable Boolean isAfter) {
      var value = cursor.values.get(order.getProperty());
      if (value == null) {
        throw new RuntimeException(
            String.format("Field[%s] does not exist in Cursor", order.getProperty()));
      }

      if (isAfter == null) {
        return handler.handle(order.getProperty(), null, value);
      } else {
        return handler.handle(
            order.getProperty(),
            (isAfter && order.isAscending()) || (!isAfter && order.isDescending()),
            value);
      }
    }

    static BooleanExpression createAdditionalFilter(
        final Cursor cursor,
        final ComparableFieldHandler handler,
        final Sort sort,
        boolean isAfter) {
      var whenNotMatching =
          Expressions.allOf(
              sort.stream()
                  .map(o -> createAdditionalFilter(cursor, handler, o, isAfter))
                  .toArray(BooleanExpression[]::new));
      var whenMatching =
          Expressions.allOf(
                  sort.stream()
                      .map(o -> createAdditionalFilter(cursor, handler, o, null))
                      .toArray(BooleanExpression[]::new))
              .and(handler.handle("id", isAfter, cursor.id));
      return whenMatching.or(whenNotMatching);
    }

    static Sort createSortBasedOnCursor(final Sort sort, boolean isAfter) {
      var orders =
          sort.stream()
              .map(
                  item -> {
                    var direction = item.getDirection();
                    if (!isAfter) {
                      direction =
                          direction.isAscending() ? Sort.Direction.DESC : Sort.Direction.ASC;
                    }
                    return new Sort.Order(direction, item.getProperty(), item.getNullHandling());
                  })
              .toList();
      return Sort.by(orders);
    }

    Map.Entry<BooleanExpression, Sort> createQuery(
        final ComparableFieldHandler handler, final Sort sort);

    record Unidirectional(@Nullable Cursor cursor, boolean isAfter, @Positive int size)
        implements Pagination {

      public @Positive Integer getSize() {
        return this.size;
      }

      @Override
      public Map.Entry<BooleanExpression, Sort> createQuery(
          final ComparableFieldHandler handler, final Sort sort) {
        return Map.entry(
            cursor == null
                ? Expressions.TRUE
                : createAdditionalFilter(cursor, handler, sort, isAfter),
            createSortBasedOnCursor(sort, isAfter));
      }
    }

    record Bidirectional(
        Cursor after, Cursor before, @Nullable @Positive Integer size, boolean isAfter)
        implements Pagination {
      @Nullable
      @Override
      public Integer getSize() {
        return this.size;
      }

      @Override
      public Map.Entry<BooleanExpression, Sort> createQuery(
          final ComparableFieldHandler handler, final Sort sort) {
        return Map.entry(
            Expressions.allOf(
                createAdditionalFilter(after, handler, sort, true),
                createAdditionalFilter(before, handler, sort, false)),
            createSortBasedOnCursor(sort, isAfter));
      }
    }
  }
}
