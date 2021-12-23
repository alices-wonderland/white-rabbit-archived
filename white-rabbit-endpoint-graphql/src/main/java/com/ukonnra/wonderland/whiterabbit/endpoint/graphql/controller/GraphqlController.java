package com.ukonnra.wonderland.whiterabbit.endpoint.graphql.controller;

import com.ukonnra.wonderland.whiterabbit.core.entity.Book;
import com.ukonnra.wonderland.whiterabbit.core.entity.User;
import com.ukonnra.wonderland.whiterabbit.core.service.BookService;
import com.ukonnra.wonderland.whiterabbit.core.service.UserService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@Slf4j
@Transactional
public class GraphqlController {
  private final UserService userService;
  private final BookService bookService;

  public GraphqlController(UserService userService, BookService bookService) {
    this.userService = userService;
    this.bookService = bookService;

    Mono.fromCallable(
            () -> {
              List<Book> books = new ArrayList<>();
              for (int i = 0; i < 2; i++) {
                var book = new Book();
                book.setName("book " + i);
                books.add(book);
              }
              var manager = new User();
              manager.setName("manager");
              manager.setBooks(new HashSet<>(books));
              return manager;
            })
        .flatMap(this.userService::save)
        .doOnNext(u -> log.info("Manager ID: {}", u.getId()))
        .flatMap(
            u -> {
              var employee = new User();
              employee.setName("employee");
              employee.setManager(u);
              return this.userService.save(employee);
            })
        .doOnNext(u -> log.info("Employee ID: {}", u.getId()))
        .subscribe();
  }

  @QueryMapping
  public Mono<User> user(@Argument UUID id) {
    log.info("Start get User[{}]", id);
    return this.userService
        .findById(id)
        .doOnNext(u -> log.info("User[{}, name = {}] get", u.getId(), u.getName()));
  }

  @SchemaMapping(typeName = "User")
  public Mono<String> name(final User user) {
    return Mono.just(user.getName());
  }

  @SchemaMapping(typeName = "User")
  public Mono<User> manager(final User user) {
    return Mono.justOrEmpty(user.getManager());
  }

  @SchemaMapping(typeName = "User")
  public Flux<Book> books(final User user) {
    log.info("Start get books for User[{}]", user.getId());
    return this.bookService
        .findAllByAuthor(user)
        .doOnNext(
            b ->
                log.info(
                    "Book[{}, name={}] get by User[{}, name={}]",
                    b.getId(),
                    b.getName(),
                    user.getId(),
                    user.getName()));
  }

  @SchemaMapping(typeName = "Book")
  public Mono<User> author(final Book book) {
    return Mono.justOrEmpty(book.getAuthor());
  }
}
