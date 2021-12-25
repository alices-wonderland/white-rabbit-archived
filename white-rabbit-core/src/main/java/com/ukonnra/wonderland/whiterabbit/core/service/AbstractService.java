package com.ukonnra.wonderland.whiterabbit.core.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.ukonnra.wonderland.whiterabbit.core.entity.AbstractEntity;
import com.ukonnra.wonderland.whiterabbit.core.query.Cursor;
import com.ukonnra.wonderland.whiterabbit.core.repository.AbstractRepository;
import java.util.LinkedList;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public abstract class AbstractService<T extends AbstractEntity, R extends AbstractRepository<T>> {
  protected final R repository;

  protected AbstractService(R repository) {
    this.repository = repository;
  }

  abstract BooleanExpression comparableFieldHandler(
      String name, @Nullable Boolean isAfter, Object value);

  public Mono<T> save(T entity) {
    return Mono.fromCallable(() -> this.repository.save(entity))
        .publishOn(Schedulers.boundedElastic());
  }

  public Mono<T> findById(UUID id) {
    return Mono.fromCallable(() -> this.repository.findById(id))
        .publishOn(Schedulers.boundedElastic())
        .flatMap(Mono::justOrEmpty);
  }

  public Flux<T> findAll(
      final BooleanExpression filter, final Sort sort, final Cursor.Pagination pagination) {
    var query = pagination.createQuery(this::comparableFieldHandler, sort);
    return Mono.fromCallable(
            () ->
                this.repository.findAll(
                    query.getKey().equals(Expressions.TRUE) ? filter : filter.and(query.getKey()),
                    query.getValue()))
        .publishOn(Schedulers.boundedElastic())
        .map(
            items -> {
              var reordered = new LinkedList<T>();
              var shouldBeReversed = !pagination.isAfter();
              for (var item : items) {
                if (shouldBeReversed) {
                  reordered.addFirst(item);
                } else {
                  reordered.addLast(item);
                }

                if (pagination.getSize() != null && reordered.size() >= pagination.getSize()) {
                  break;
                }
              }
              return reordered;
            })
        .flatMapMany(Flux::fromIterable);
  }
}
