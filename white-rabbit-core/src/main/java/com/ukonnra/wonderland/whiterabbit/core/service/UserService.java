package com.ukonnra.wonderland.whiterabbit.core.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.ukonnra.wonderland.whiterabbit.core.entity.QUser;
import com.ukonnra.wonderland.whiterabbit.core.entity.User;
import com.ukonnra.wonderland.whiterabbit.core.repository.UserRepository;
import java.util.UUID;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public final class UserService extends AbstractService<User, UserRepository> {
  public UserService(UserRepository repository) {
    super(repository);
  }

  @Override
  BooleanExpression comparableFieldHandler(String name, @Nullable Boolean isAfter, Object value) {
    if (name.equals("id")) {
      return doCompare(
          isAfter,
          QUser.user.id::gt,
          QUser.user.id::lt,
          QUser.user.id::eq,
          UUID.fromString(value.toString()));
    } else if (name.equals("name")) {
      return doCompare(
          isAfter, QUser.user.name::gt, QUser.user.name::lt, QUser.user.name::eq, value.toString());
    } else if (name.equals("version") && value instanceof Number item) {
      return doCompare(
          isAfter,
          QUser.user.version::gt,
          QUser.user.version::lt,
          QUser.user.version::eq,
          item.longValue());
    } else {
      throw new RuntimeException(String.format("Field[%s] is not sortable", name));
    }
  }
}
