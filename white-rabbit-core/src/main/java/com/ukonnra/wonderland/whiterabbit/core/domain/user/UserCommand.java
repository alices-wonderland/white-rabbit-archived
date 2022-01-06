package com.ukonnra.wonderland.whiterabbit.core.domain.user;

import com.ukonnra.wonderland.whiterabbit.core.domain.user.valobj.Identifier;
import java.util.Set;
import java.util.UUID;
import org.springframework.lang.Nullable;

public sealed interface UserCommand {
  record Create(@Nullable UUID id, String name, User.Role role, Set<Identifier> identifiers)
      implements UserCommand {
    public Create(String name, User.Role role, Set<Identifier> identifiers) {
      this(null, name, role, identifiers);
    }
  }

  record Update(
      UUID id,
      @Nullable String name,
      @Nullable User.Role role,
      @Nullable Set<Identifier> identifiers)
      implements UserCommand {}

  record Delete(UUID id) implements UserCommand {}
}
