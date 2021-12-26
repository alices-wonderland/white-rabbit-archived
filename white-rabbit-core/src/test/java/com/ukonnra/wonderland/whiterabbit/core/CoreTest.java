package com.ukonnra.wonderland.whiterabbit.core;

import com.ukonnra.wonderland.whiterabbit.core.entity.Book;
import com.ukonnra.wonderland.whiterabbit.core.entity.QUser;
import com.ukonnra.wonderland.whiterabbit.core.entity.User;
import com.ukonnra.wonderland.whiterabbit.core.query.Cursor;
import com.ukonnra.wonderland.whiterabbit.core.repository.BookRepository;
import com.ukonnra.wonderland.whiterabbit.core.repository.UserRepository;
import com.ukonnra.wonderland.whiterabbit.core.service.BookService;
import com.ukonnra.wonderland.whiterabbit.core.service.UserService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@Slf4j
class CoreTest {
  @SpringBootConfiguration
  @EnableAutoConfiguration
  @Import({CoreConfiguration.class})
  static class Configuration {}

  private final BookRepository bookRepository;
  private final UserRepository userRepository;

  private final BookService bookService;
  private final UserService userService;

  @Container
  private static final PostgreSQLContainer postgreSQLContainer =
      new PostgreSQLContainer("postgres:14-alpine");

  @DynamicPropertySource
  private static void postgresqlProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
    registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
  }

  @Autowired
  public CoreTest(
      BookRepository bookRepository,
      UserRepository userRepository,
      BookService bookService,
      UserService userService) {
    this.bookRepository = bookRepository;
    this.userRepository = userRepository;
    this.bookService = bookService;
    this.userService = userService;
  }

  @Test
  @Transactional
  void testSuccessJPAAndQueryDSL() {
    List<Book> books = new ArrayList<>();
    for (int i = 0; i < 2; i++) {
      var book = new Book();
      book.setName("book " + i);
      books.add(book);
    }

    var manager = new User();
    manager.setName("manager");
    manager.setBooks(new HashSet<>(books));
    this.userRepository.saveAndFlush(manager);
    var employee = new User();
    employee.setName("employee");
    employee.setManager(manager);
    this.userRepository.saveAndFlush(employee);

    for (var user : List.of(manager, employee)) {
      log.info("User[name={}] create date = {}", user.getName(), user.getCreatedDate());
    }

    Assertions.assertThat(manager.getBooks())
        .hasSize(2)
        .allMatch(book -> book.getAuthor().equals(manager));

    for (var book : books) {
      log.info(
          "Book[id={}, name={}, author={id={}, name={}}] create date = {}, last modified = {}, with version = {}",
          book.getId(),
          book.getName(),
          book.getAuthor().getId(),
          book.getAuthor().getName(),
          book.getCreatedDate(),
          book.getLastModifiedDate(),
          book.getVersion());
    }

    var book = books.get(0);
    var oldVersion = book.getVersion();
    book.setName("New book");
    this.bookRepository.saveAndFlush(book);

    log.info(
        "NewBook[name={}, author={id={}, name={}}] create date = {}, last modified = {}, with version = {}",
        book.getName(),
        book.getAuthor().getId(),
        book.getAuthor().getName(),
        book.getCreatedDate(),
        book.getLastModifiedDate(),
        book.getVersion());

    Assertions.assertThat(books.get(0).getName()).isEqualTo("New book");
    Assertions.assertThat(book.getVersion()).isGreaterThan(oldVersion);

    manager.getBooks().removeIf(b -> Objects.equals(b.getId(), book.getId()));
    this.userRepository.saveAndFlush(manager);
    Assertions.assertThat(this.bookRepository.existsById(books.get(1).getId())).isTrue();
    Assertions.assertThat(this.bookRepository.existsById(books.get(0).getId())).isFalse();
    Assertions.assertThat(manager.getBooks()).hasSize(1);
  }

  @Test
  void testSuccessPaginationQuery() {
    var users =
        IntStream.range(0, 5)
            .mapToObj(
                i -> {
                  var user = new User();
                  user.setName("User " + i);
                  user.setVersion((long) i);
                  return user;
                })
            .toList();
    this.userRepository.saveAllAndFlush(users);

    var targetUser = users.get(2);

    Assertions.assertThat(targetUser.getId()).isNotNull();

    var resultQueryBefore =
        this.userService
            .findAll(
                QUser.user.name.startsWith("User"),
                Sort.by("name"),
                new Cursor.Pagination.Unidirectional(
                    new Cursor(targetUser.getId(), Map.of("name", targetUser.getName())), false, 3))
            .collectList()
            .block();
    Assertions.assertThat(resultQueryBefore).isEqualTo(users.subList(0, 2));

    var resultQueryAfter =
        this.userService
            .findAll(
                QUser.user.name.startsWith("User"),
                Sort.by("name"),
                new Cursor.Pagination.Unidirectional(
                    new Cursor(targetUser.getId(), Map.of("name", targetUser.getName())), true, 3))
            .collectList()
            .block();
    Assertions.assertThat(resultQueryAfter).isEqualTo(users.subList(3, 5));

    var resultQueryHead =
        this.userService
            .findAll(
                QUser.user.name.startsWith("User"),
                Sort.by("name"),
                new Cursor.Pagination.Unidirectional(null, true, 3))
            .collectList()
            .block();
    Assertions.assertThat(resultQueryHead).isEqualTo(users.subList(0, 3));

    var resultQueryTail =
        this.userService
            .findAll(
                QUser.user.name.startsWith("User"),
                Sort.by("name"),
                new Cursor.Pagination.Unidirectional(null, false, 3))
            .collectList()
            .block();
    Assertions.assertThat(resultQueryTail).isEqualTo(users.subList(2, 5));

    var resultQueryBetween =
        this.userService
            .findAll(
                QUser.user.name.startsWith("User"),
                Sort.by("name"),
                new Cursor.Pagination.Bidirectional(
                    new Cursor(users.get(0).getId(), Map.of("name", users.get(0).getName())),
                    new Cursor(users.get(4).getId(), Map.of("name", users.get(4).getName())),
                    2,
                    true))
            .collectList()
            .block();
    Assertions.assertThat(resultQueryBetween).isEqualTo(users.subList(1, 3));

    var resultQueryBetweenBefore =
        this.userService
            .findAll(
                QUser.user.name.startsWith("User"),
                Sort.by("name"),
                new Cursor.Pagination.Bidirectional(
                    new Cursor(users.get(0).getId(), Map.of("name", users.get(0).getName())),
                    new Cursor(users.get(4).getId(), Map.of("name", users.get(4).getName())),
                    2,
                    false))
            .collectList()
            .block();
    Assertions.assertThat(resultQueryBetweenBefore).isEqualTo(users.subList(2, 4));

    var resultQueryBetweenReversed =
        this.userService
            .findAll(
                QUser.user.name.startsWith("User"),
                Sort.by(Sort.Direction.DESC, "name"),
                new Cursor.Pagination.Bidirectional(
                    new Cursor(users.get(4).getId(), Map.of("name", users.get(4).getName())),
                    new Cursor(users.get(0).getId(), Map.of("name", users.get(0).getName())),
                    2,
                    true))
            .collectList()
            .block();
    Assertions.assertThat(resultQueryBetweenReversed)
        .isEqualTo(List.of(users.get(3), users.get(2)));
  }
}
