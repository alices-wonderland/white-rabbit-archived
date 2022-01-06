package com.ukonnra.wonderland.whiterabbit.core.domain.journal;

import java.util.UUID;

public interface JournalEvent {
  record Deleted(UUID id) implements JournalEvent {
    public Deleted(final Journal journal) {
      this(journal.getId());
    }
  }
}
