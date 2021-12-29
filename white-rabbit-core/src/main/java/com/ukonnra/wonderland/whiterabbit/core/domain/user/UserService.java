package com.ukonnra.wonderland.whiterabbit.core.domain.user;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.ukonnra.wonderland.whiterabbit.core.infrastructure.AbstractService;
import java.util.UUID;
import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class UserService extends AbstractService<User, UserRepository, User.PresentationModel> {
  protected UserService(UserRepository repository) {
    super(repository);
  }

  @Override
  public <V> V getProtectedFieldById(UserDetails user, UUID id, String field) {
    return null;
  }

  @Override
  public BooleanExpression createPaginationItemFilterByField(
      UUID id, String field, @Nullable Boolean isAfter) {
    return null;
  }
}
