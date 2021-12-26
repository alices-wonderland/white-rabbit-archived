package com.ukonnra.wonderland.whiterabbit.core.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.ukonnra.wonderland.whiterabbit.core.entity.Book;
import com.ukonnra.wonderland.whiterabbit.core.entity.QBook;
import com.ukonnra.wonderland.whiterabbit.core.repository.BookRepository;
import java.util.UUID;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public final class BookService extends AbstractService<Book, BookRepository> {
  public BookService(BookRepository repository) {
    super(repository);
  }

  @Override
  BooleanExpression comparableFieldHandler(String name, @Nullable Boolean isAfter, Object value) {
    if (name.equals("id")) {
      return doCompare(
          isAfter,
          QBook.book.id::gt,
          QBook.book.id::lt,
          QBook.book.id::eq,
          UUID.fromString(value.toString()));
    } else if (name.equals("name")) {
      return doCompare(
          isAfter, QBook.book.name::gt, QBook.book.name::lt, QBook.book.name::eq, value.toString());
    } else if (name.equals("version") && value instanceof Number item) {
      return doCompare(
          isAfter,
          QBook.book.version::gt,
          QBook.book.version::lt,
          QBook.book.version::eq,
          item.longValue());
    } else {
      throw new RuntimeException(String.format("Field[%s] is not sortable", name));
    }
  }
}
