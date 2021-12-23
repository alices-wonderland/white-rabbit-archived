package com.ukonnra.wonderland.whiterabbit.core.repository;

import com.ukonnra.wonderland.whiterabbit.core.entity.Book;
import com.ukonnra.wonderland.whiterabbit.core.entity.User;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface BookRepository extends JpaRepository<Book, UUID>, QuerydslPredicateExecutor<Book> {
  List<Book> findAllByAuthor(final User author);
}
