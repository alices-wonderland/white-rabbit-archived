package com.ukonnra.wonderland.whiterabbit.core.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.ukonnra.wonderland.whiterabbit.core.CoreException;
import com.ukonnra.wonderland.whiterabbit.core.entity.QUser;
import com.ukonnra.wonderland.whiterabbit.core.entity.User;
import com.ukonnra.wonderland.whiterabbit.core.repository.UserRepository;
import java.util.UUID;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class UserService extends AbstractService<User, UserRepository> {
  public UserService(UserRepository repository) {
    super(repository);
  }

  @Override
  BooleanExpression createPaginationItemFilterByField(
      UUID id, String field, @Nullable Boolean isAfter) {
    var user =
        this.repository.findById(id).orElseThrow(() -> new CoreException.NotFound(User.class, id));
    return switch (field) {
      case "id" -> doCompare(isAfter, QUser.user.id, user.getId());
      case "name" -> doCompare(isAfter, QUser.user.name, user.getName());
      case "version" -> doCompare(isAfter, QUser.user.version, user.getVersion());
      default -> throw new CoreException.FieldNotSortable(User.class, field);
    };
  }
}
