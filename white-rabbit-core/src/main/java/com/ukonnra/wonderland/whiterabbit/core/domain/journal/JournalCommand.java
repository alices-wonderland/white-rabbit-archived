package com.ukonnra.wonderland.whiterabbit.core.domain.journal;

import com.ukonnra.wonderland.whiterabbit.core.domain.journal.entity.Account;
import com.ukonnra.wonderland.whiterabbit.core.domain.journal.valobj.FinRecordItemInput;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.springframework.lang.Nullable;

public sealed interface JournalCommand {
  record Create(@Nullable UUID id, String name, Set<UUID> adminIds, Set<UUID> memberIds)
      implements JournalCommand {}

  record Update(
      UUID id, @Nullable String name, @Nullable Set<UUID> adminIds, @Nullable Set<UUID> memberIds)
      implements JournalCommand {}

  record Delete(UUID id) implements JournalCommand {}

  record CreateAccount(
      UUID journalId,
      @Nullable UUID id,
      String name,
      String description,
      Account.Type type,
      String unit,
      Account.InventoryType inventoryType)
      implements JournalCommand {}

  record UpdateAccount(
      UUID id,
      @Nullable String name,
      @Nullable String description,
      @Nullable Account.Type type,
      @Nullable String unit,
      @Nullable Account.InventoryType inventoryType)
      implements JournalCommand {}

  record DeleteAccount(UUID id) implements JournalCommand {}

  record CreateFinRecord(
      UUID journalId,
      @Nullable UUID id,
      String name,
      String description,
      Instant happenedAt,
      boolean isContingent,
      Set<FinRecordItemInput> items,
      Set<String> tags)
      implements JournalCommand {}

  record UpdateFinRecord(
      UUID id,
      @Nullable String name,
      @Nullable String description,
      @Nullable Instant happenedAt,
      @Nullable Boolean isContingent,
      @Nullable Set<FinRecordItemInput> items,
      @Nullable Set<String> tags)
      implements JournalCommand {}
}
