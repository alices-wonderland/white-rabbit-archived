package com.ukonnra.wonderland.whiterabbit.core.command;

import java.util.UUID;
import org.springframework.lang.Nullable;

public sealed interface BookCommand
    permits BookCommand.ChangeAuthor, BookCommand.Create, BookCommand.Delete, BookCommand.Update {
  record Create(@Nullable UUID id, String name, UUID authorId) implements BookCommand {}

  record Update(UUID id, String name) implements BookCommand {}

  record ChangeAuthor(UUID id, UUID newAuthorId) implements BookCommand {}

  record Delete(UUID id) implements BookCommand {}
}
