package com.ukonnra.wonderland.whiterabbit.core.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.ukonnra.wonderland.whiterabbit.core.entity.User;
import com.ukonnra.wonderland.whiterabbit.core.repository.UserRepository;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public final class UserService extends AbstractService<User, UserRepository> {
  public UserService(UserRepository repository) {
    super(repository);
  }

  @Override
  BooleanExpression comparableFieldHandler(String name, @Nullable Boolean isAfter, Object value) {
    return User.comparableFieldHandler(name, isAfter, value);
  }
}
