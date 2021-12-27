package com.ukonnra.wonderland.whiterabbit.endpoint.graphql.controller;

import com.ukonnra.wonderland.whiterabbit.core.entity.Book;
import com.ukonnra.wonderland.whiterabbit.core.entity.QBook;
import com.ukonnra.wonderland.whiterabbit.core.entity.User;
import com.ukonnra.wonderland.whiterabbit.core.query.CursorPage;
import com.ukonnra.wonderland.whiterabbit.core.query.Pagination;
import com.ukonnra.wonderland.whiterabbit.core.service.BookService;
import com.ukonnra.wonderland.whiterabbit.core.service.UserService;
import com.ukonnra.wonderland.whiterabbit.endpoint.graphql.ApplicationException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
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
              for (int i = 0; i < 10; i++) {
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

  private record OrderDTO(String property, Sort.Direction direction) {}

  private Pagination createPaginationWithSize(
      final String after, final String before, int size, boolean isAfter) {
    return new Pagination(after, before, size, isAfter);
  }

  @SchemaMapping(typeName = "User")
  public Mono<CursorPage<Book>> books(
      final User user,
      @Argument @Nullable Integer first,
      @Argument @Nullable String after,
      @Argument @Nullable Integer last,
      @Argument @Nullable String before,
      @Argument String filter,
      @Argument List<OrderDTO> sort) {
    Pagination pagination;
    if (first != null && last != null) {
      throw new ApplicationException.ExclusiveFirstAndLastFields();
    } else if (first != null) {
      pagination = createPaginationWithSize(after, before, first, true);
    } else if (last != null) {
      pagination = createPaginationWithSize(after, before, last, false);
    } else {
      pagination = createPaginationWithSize(after, before, 2, true);
    }

    return this.bookService.findAll(
        QBook.book.name.startsWith("book"),
        Sort.by(sort.stream().map(o -> new Sort.Order(o.direction, o.property)).toList()),
        pagination);
  }

  @SchemaMapping(typeName = "Book")
  public Mono<User> author(final Book book) {
    return Mono.justOrEmpty(book.getAuthor());
  }
}
