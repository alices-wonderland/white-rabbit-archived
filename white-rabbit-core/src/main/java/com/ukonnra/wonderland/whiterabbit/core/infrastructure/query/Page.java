package com.ukonnra.wonderland.whiterabbit.core.infrastructure.query;

import com.ukonnra.wonderland.whiterabbit.core.infrastructure.AbstractEntity;
import com.ukonnra.wonderland.whiterabbit.core.infrastructure.AbstractPresentationModel;
import java.util.LinkedList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

/*
Error when using records with generics:
  Caused by: java.lang.NullPointerException:Cannot invoke
  "net.bytebuddy.description.TypeVariableSource.findVariable(String)" because "typeVariableSource" is null
 */
@Value
@AllArgsConstructor
@Slf4j
public class Page<PreT extends AbstractPresentationModel> {
  PageInfo pageInfo;
  List<Item<PreT>> items;

  public Page() {
    this.pageInfo = new PageInfo();
    this.items = List.of();
  }

  public record PageInfo(
      boolean hasPreviousPage,
      boolean hasNextPage,
      @Nullable String firstCursor,
      @Nullable String lastCursor) {
    public PageInfo() {
      this(false, false, null, null);
    }
  }

  @Value
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Item<PreT extends AbstractPresentationModel> {
    @Nullable String cursor;
    PreT data;

    public static <E extends AbstractEntity<PreT>, PreT extends AbstractPresentationModel>
        Item<PreT> of(final E entity) {
      return new Item<>(Cursor.of(entity).orElse(null), entity.toPresentationModel());
    }
  }

  /**
   *
   *
   * <h2>How to calculate {@code hasPreviousPage} and {@code hasNextPage}</h2>
   *
   * <ul>
   *   <li>If {@code pagination instanceof Bidirectional}, then {@code hasNextPage == true} and
   *       {@code hasPreviousPage == true}
   *   <li>Else if {@code isAfter == true}:
   *       <ul>
   *         <li>If {@code items.size > targetSize}, {@code hasNextPage == true}; otherwise, {@code
   *             false}
   *         <li>If the after cursor exists: <strong>{@code hasPreviousPage == true}</strong>, since
   *             there are at least one entity before the result: the after cursor
   *         <li>Else, <strong>{@code hasPreviousPage == false}</strong>
   *       </ul>
   *   <li>Else, meaning the query is from the bottom:
   *       <ul>
   *         <li>If {@code items.size > targetSize}, {@code hasPreviousPage == true}; otherwise,
   *             {@code false}
   *         <li>If the before cursor exists: <strong>{@code hasNextPage == true}</strong>, since
   *             there are at least one entity before the result: the before cursor
   *         <li>Else, <strong>{@code before == false}</strong>
   *       </ul>
   * </ul>
   *
   * @param entities The filter result from the database, expecting containing {@code size + 1}
   *     entities
   * @param pagination The pagination from the query request
   * @param <E> The entity class
   * @param <PreT> The presentation model class
   * @return The pagination result
   */
  public static <E extends AbstractEntity<PreT>, PreT extends AbstractPresentationModel>
      Page<PreT> of(List<E> entities, Pagination pagination) {
    var entitiesExceedingTarget = entities.size() > pagination.size();

    // When exceeding, meaning there are next page or previous page
    if (entitiesExceedingTarget) {
      entities = entities.subList(0, entities.size() - 1);
    }

    E first = null, last = null;
    if (entities.size() > 0) {
      first = entities.get(0);
      last = entities.get(entities.size() - 1);
    }

    if (!pagination.isAfter()) {
      var temp = first;
      first = last;
      last = temp;
    }

    if (first == null && last == null) {
      return new Page<>();
    }

    boolean hasPreviousPage, hasNextPage;
    if (pagination.before() != null && pagination.after() != null) {
      hasPreviousPage = true;
      hasNextPage = true;
    } else if (pagination.isAfter()) {
      hasPreviousPage = pagination.after() != null;
      hasNextPage = entitiesExceedingTarget;
    } else {
      hasPreviousPage = entitiesExceedingTarget;
      hasNextPage = pagination.before() != null;
    }

    var items = new LinkedList<Item<PreT>>();
    for (var e : entities) {
      if (pagination.isAfter()) {
        items.addLast(Item.of(e));
      } else {
        items.addFirst(Item.of(e));
      }
    }

    return new Page<>(
        new PageInfo(
            hasPreviousPage,
            hasNextPage,
            Cursor.of(first).orElse(null),
            Cursor.of(last).orElse(null)),
        items);
  }
}
