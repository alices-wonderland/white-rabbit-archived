package com.ukonnra.wonderland.whiterabbit.core;

import com.ukonnra.wonderland.whiterabbit.core.entity.Book;
import com.ukonnra.wonderland.whiterabbit.core.entity.QUser;
import com.ukonnra.wonderland.whiterabbit.core.entity.User;
import com.ukonnra.wonderland.whiterabbit.core.query.Cursor;
import com.ukonnra.wonderland.whiterabbit.core.query.CursorPage;
import com.ukonnra.wonderland.whiterabbit.core.query.Pagination;
import com.ukonnra.wonderland.whiterabbit.core.repository.BookRepository;
import com.ukonnra.wonderland.whiterabbit.core.repository.UserRepository;
import com.ukonnra.wonderland.whiterabbit.core.service.BookService;
import com.ukonnra.wonderland.whiterabbit.core.service.UserService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    {
      var pageBefore =
          this.userService
              .findAll(
                  QUser.user.name.startsWith("User"),
                  Sort.by("name"),
                  new Pagination(null, Cursor.of(targetUser), 3, false))
              .block();
      Assertions.assertThat(pageBefore)
          .isNotNull()
          .extracting(CursorPage::getItemContents)
          .isEqualTo(users.subList(0, 2));
      Assertions.assertThat(pageBefore.getPageInfo())
          .isEqualTo(
              new CursorPage.PageInfo(
                  false, true, Cursor.of(users.get(0)), Cursor.of(users.get(1))));
    }

    {
      var pageAfter =
          this.userService
              .findAll(
                  QUser.user.name.startsWith("User"),
                  Sort.by("name"),
                  new Pagination(Cursor.of(targetUser), null, 3, true))
              .block();
      Assertions.assertThat(pageAfter)
          .isNotNull()
          .extracting(CursorPage::getItemContents)
          .isEqualTo(users.subList(3, 5));
      Assertions.assertThat(pageAfter.getPageInfo())
          .isEqualTo(
              new CursorPage.PageInfo(
                  true, false, Cursor.of(users.get(3)), Cursor.of(users.get(4))));
    }

    {
      var pageHead =
          this.userService
              .findAll(
                  QUser.user.name.startsWith("User"),
                  Sort.by("name"),
                  new Pagination(null, null, 3, true))
              .block();
      Assertions.assertThat(pageHead)
          .isNotNull()
          .extracting(CursorPage::getItemContents)
          .isEqualTo(users.subList(0, 3));
      Assertions.assertThat(pageHead.getPageInfo())
          .isEqualTo(
              new CursorPage.PageInfo(
                  false, true, Cursor.of(users.get(0)), Cursor.of(users.get(2))));
    }
    {
      var pageTail =
          this.userService
              .findAll(
                  QUser.user.name.startsWith("User"),
                  Sort.by("name"),
                  new Pagination(null, null, 3, false))
              .block();
      Assertions.assertThat(pageTail)
          .isNotNull()
          .extracting(CursorPage::getItemContents)
          .isEqualTo(users.subList(2, 5));
      Assertions.assertThat(pageTail.getPageInfo())
          .isEqualTo(
              new CursorPage.PageInfo(
                  true, false, Cursor.of(users.get(2)), Cursor.of(users.get(4))));
    }
    {
      var pageBetween =
          this.userService
              .findAll(
                  QUser.user.name.startsWith("User"),
                  Sort.by("name"),
                  new Pagination(Cursor.of(users.get(0)), Cursor.of(users.get(4)), 2, true))
              .block();
      Assertions.assertThat(pageBetween)
          .isNotNull()
          .extracting(CursorPage::getItemContents)
          .isEqualTo(users.subList(1, 3));
      Assertions.assertThat(pageBetween.getPageInfo())
          .isEqualTo(
              new CursorPage.PageInfo(
                  true, true, Cursor.of(users.get(1)), Cursor.of(users.get(2))));
    }
    {
      var pageBetweenBefore =
          this.userService
              .findAll(
                  QUser.user.name.startsWith("User"),
                  Sort.by("name"),
                  new Pagination(Cursor.of(users.get(0)), Cursor.of(users.get(4)), 2, false))
              .block();
      Assertions.assertThat(pageBetweenBefore)
          .isNotNull()
          .extracting(CursorPage::getItemContents)
          .isEqualTo(users.subList(2, 4));
      Assertions.assertThat(pageBetweenBefore.getPageInfo())
          .isEqualTo(
              new CursorPage.PageInfo(
                  true, true, Cursor.of(users.get(2)), Cursor.of(users.get(3))));
    }
    {
      var pageBetweenReversed =
          this.userService
              .findAll(
                  QUser.user.name.startsWith("User"),
                  Sort.by(Sort.Direction.DESC, "name"),
                  new Pagination(Cursor.of(users.get(4)), Cursor.of(users.get(0)), 2, true))
              .block();
      Assertions.assertThat(pageBetweenReversed)
          .isNotNull()
          .extracting(CursorPage::getItemContents)
          .isEqualTo(List.of(users.get(3), users.get(2)));
      Assertions.assertThat(pageBetweenReversed.getPageInfo())
          .isEqualTo(
              new CursorPage.PageInfo(
                  true, true, Cursor.of(users.get(3)), Cursor.of(users.get(2))));
    }
    {
      var pageEmpty =
          this.userService
              .findAll(
                  QUser.user.name.startsWith("User"),
                  Sort.by(Sort.Direction.DESC, "name"),
                  new Pagination(Cursor.of(users.get(0)), Cursor.of(users.get(4)), 2, true))
              .block();
      Assertions.assertThat(pageEmpty).isNotNull();
      Assertions.assertThat(pageEmpty.getItemContents()).isEmpty();
      Assertions.assertThat(pageEmpty.getPageInfo())
          .isEqualTo(new CursorPage.PageInfo(false, false, null, null));
    }
  }
}
