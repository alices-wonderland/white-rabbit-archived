package com.ukonnra.wonderland.whiterabbit.core.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.ukonnra.wonderland.whiterabbit.core.CoreException;
import com.ukonnra.wonderland.whiterabbit.core.entity.Book;
import com.ukonnra.wonderland.whiterabbit.core.entity.QBook;
import com.ukonnra.wonderland.whiterabbit.core.repository.BookRepository;
import java.util.UUID;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class BookService extends AbstractService<Book, BookRepository> {
  public BookService(BookRepository repository) {
    super(repository);
  }

  @Override
  BooleanExpression createPaginationItemFilterByField(
      UUID id, String field, @Nullable Boolean isAfter) {
    var book =
        this.repository.findById(id).orElseThrow(() -> new CoreException.NotFound(Book.class, id));
    var qBook = QBook.book;

    return switch (field) {
      case "id" -> doCompare(isAfter, qBook.id, book.getId());
      case "name" -> doCompare(isAfter, qBook.name, book.getName());
      case "version" -> doCompare(isAfter, qBook.version, book.getVersion());
      default -> throw new CoreException.FieldNotSortable(Book.class, field);
    };
  }
}
