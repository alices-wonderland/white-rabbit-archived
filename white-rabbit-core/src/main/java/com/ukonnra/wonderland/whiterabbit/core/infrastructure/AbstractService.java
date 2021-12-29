package com.ukonnra.wonderland.whiterabbit.core.infrastructure;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.ukonnra.wonderland.whiterabbit.core.infrastructure.query.Page;
import com.ukonnra.wonderland.whiterabbit.core.infrastructure.query.Pagination;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Transactional
public abstract class AbstractService<
    T extends AbstractEntity<PreT>,
    R extends AbstractRepository<T, PreT>,
    PreT extends AbstractPresentationModel> {
  protected final R repository;

  protected AbstractService(R repository) {
    this.repository = repository;
  }

  public abstract <V> V getProtectedFieldById(
      final UserDetails user, final UUID id, final String field);

  public abstract BooleanExpression createPaginationItemFilterByField(
      UUID id, String field, @Nullable Boolean isAfter);

  protected static <T extends Number & Comparable<T>> BooleanExpression doCreateFilter(
      @Nullable Boolean isAfter, NumberExpression<T> expression, T value) {
    if (isAfter == null) {
      return expression.eq(value);
    } else if (isAfter) {
      return expression.gt(value);
    } else {
      return expression.lt(value);
    }
  }

  protected static <T extends Comparable<T>> BooleanExpression doCompare(
      @Nullable Boolean isAfter, ComparableExpression<T> expression, T value) {
    if (isAfter == null) {
      return expression.eq(value);
    } else if (isAfter) {
      return expression.gt(value);
    } else {
      return expression.lt(value);
    }
  }

  public Mono<PreT> save(T entity) {
    return Mono.fromCallable(() -> this.repository.save(entity))
        .subscribeOn(Schedulers.boundedElastic())
        .map(T::toPresentationModel);
  }

  public Mono<PreT> findById(UUID id) {
    return Mono.fromCallable(() -> this.repository.findById(id))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(Mono::justOrEmpty)
        .map(T::toPresentationModel);
  }

  /**
   * Find all entities with the provided filter, sort and pagination parameters.
   *
   * <p>Since all findAll queries are based on Cursor-Based Pagination, the algorithm:
   *
   * <ul>
   *   <li><strong>Filter</strong> based on {@code filter}
   *   <li><strong>Sort</strong> based on {@code sort}
   *   <li><strong>Pagination filter</strong> based on {@code pagination}
   *       <ul>
   *         <li>If the pagination is unidirectional:
   *             <ul>
   *               <li>If searching entities <strong>after</strong> the cursor:
   *                   <ul>
   *                     <li><strong>Filter IN</strong> the entities <strong>matching</strong> the
   *                         sort direction
   *                   </ul>
   *               <li>Else, <strong>revert</strong> the sort order, then filter matching the sort
   *                   direction
   *             </ul>
   *         <li>If bidirectional:
   *             <ul>
   *               <li>For cursor {@code before}, like the unidirectional, filter entities
   *                   <strong>before</strong> the cursor
   *               <li>For cursor {@code after}, after
   *             </ul>
   *         <li>If filtering by the <strong>before</strong> cursor, since the sort direction has
   *             been reverted, the result should be reverted as well
   *       </ul>
   * </ul>
   *
   * @param filter The basic filter parameter
   * @param sort All sorting fields are comparable
   * @param pagination The cursor-based pagination info
   * @return Entities pagination result
   */
  public Mono<Page<PreT>> findAll(
      final BooleanExpression filter, final Sort sort, final Pagination pagination) {
    var paginationFilter = pagination.createFilter(this::createPaginationItemFilterByField, sort);
    var paginationSort = pagination.createSort(sort);
    return Mono.fromCallable(
            () ->
                this.repository.findAll(
                    paginationFilter == null ? filter : filter.and(paginationFilter),
                    PageRequest.ofSize(pagination.size() + 1).withSort(paginationSort)))
        .subscribeOn(Schedulers.boundedElastic())
        .map(entities -> Page.of(entities.getContent(), pagination));
  }
}
