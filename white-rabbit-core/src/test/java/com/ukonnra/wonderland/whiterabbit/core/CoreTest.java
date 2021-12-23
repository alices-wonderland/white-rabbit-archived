package com.ukonnra.wonderland.whiterabbit.core;

import com.ukonnra.wonderland.whiterabbit.core.entity.Book;
import com.ukonnra.wonderland.whiterabbit.core.entity.User;
import com.ukonnra.wonderland.whiterabbit.core.repository.BookRepository;
import com.ukonnra.wonderland.whiterabbit.core.repository.UserRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ExtendWith(SpringExtension.class)
@TestComponent
@Testcontainers
@Slf4j
class CoreTest {
  @TestConfiguration
  @EnableAutoConfiguration
  @Import({CoreConfiguration.class})
  static class Configuration {}

  private final BookRepository bookRepository;
  private final UserRepository userRepository;

  @Container
  private static final PostgreSQLContainer postgreSQLContainer =
      new PostgreSQLContainer("postgres:14-alpine");

  @DynamicPropertySource
  private static void postgresqlProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
    registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
    registry.add("spring.jpa.show-sql", () -> true);
  }

  @Autowired
  public CoreTest(BookRepository bookRepository, UserRepository userRepository) {
    this.bookRepository = bookRepository;
    this.userRepository = userRepository;
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
}
