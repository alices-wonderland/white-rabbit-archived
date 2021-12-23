package com.ukonnra.wonderland.whiterabbit.core.service;

import com.ukonnra.wonderland.whiterabbit.core.entity.Book;
import com.ukonnra.wonderland.whiterabbit.core.entity.User;
import com.ukonnra.wonderland.whiterabbit.core.repository.BookRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public final class BookService {
  private final BookRepository repository;

  public BookService(BookRepository repository) {
    this.repository = repository;
  }

  public Mono<Book> save(final Book book) {
    return Mono.fromCallable(() -> this.repository.save(book))
        .publishOn(Schedulers.boundedElastic());
  }

  public Mono<Book> findById(final UUID id) {
    return Mono.fromCallable(() -> this.repository.findById(id))
        .publishOn(Schedulers.boundedElastic())
        .flatMap(Mono::justOrEmpty);
  }

  public Flux<Book> findAllByAuthor(final User author) {
    return Mono.fromCallable(() -> this.repository.findAllByAuthor(author))
        .publishOn(Schedulers.boundedElastic())
        .flatMapIterable(l -> l);
  }
}
