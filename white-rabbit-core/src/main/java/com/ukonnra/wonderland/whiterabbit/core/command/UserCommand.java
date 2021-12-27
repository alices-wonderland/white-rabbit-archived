package com.ukonnra.wonderland.whiterabbit.core.command;

import java.util.Set;
import java.util.UUID;
import org.springframework.lang.Nullable;

public sealed interface UserCommand extends AbstractCommand
    permits UserCommand.ChangeManager, UserCommand.Create, UserCommand.Delete, UserCommand.Update {
  record Create(@Nullable UUID id, String name, @Nullable UUID managerId) implements UserCommand {}

  record Update(UUID id, String name, Set<UUID> bookIds) implements UserCommand {}

  record ChangeManager(UUID id, UUID newManagerId) implements UserCommand {}

  record Delete(UUID id) implements UserCommand {}
}
