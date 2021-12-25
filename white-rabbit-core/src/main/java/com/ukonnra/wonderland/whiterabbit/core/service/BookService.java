package com.ukonnra.wonderland.whiterabbit.core.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.ukonnra.wonderland.whiterabbit.core.entity.Book;
import com.ukonnra.wonderland.whiterabbit.core.repository.BookRepository;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public final class BookService extends AbstractService<Book, BookRepository> {
  public BookService(BookRepository repository) {
    super(repository);
  }

  @Override
  BooleanExpression comparableFieldHandler(String name, @Nullable Boolean isAfter, Object value) {
    return Book.comparableFieldHandler(name, isAfter, value);
  }
}
