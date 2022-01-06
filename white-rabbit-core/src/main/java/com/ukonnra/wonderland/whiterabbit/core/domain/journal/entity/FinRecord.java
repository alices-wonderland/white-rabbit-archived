package com.ukonnra.wonderland.whiterabbit.core.domain.journal.entity;

import com.ukonnra.wonderland.whiterabbit.core.domain.journal.Journal;
import com.ukonnra.wonderland.whiterabbit.core.domain.journal.valobj.FinRecordItemInput;
import com.ukonnra.wonderland.whiterabbit.core.infrastructure.AbstractEntity;
import com.ukonnra.wonderland.whiterabbit.core.infrastructure.AbstractPresentationModel;
import com.ukonnra.wonderland.whiterabbit.core.infrastructure.CoreException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.lang.Nullable;

@Entity(name = "finRecords")
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@SuppressWarnings("squid:S2160")
public final class FinRecord extends AbstractEntity<FinRecord.PresentationModel> {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @ToString.Exclude
  private Journal journal;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String description;

  @Column(nullable = false)
  private Instant happenedAt;

  @Column(nullable = false)
  private boolean isContingent = false;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "finRecord", orphanRemoval = true)
  @ToString.Exclude
  private Set<FinRecordItem> items = new HashSet<>();

  @ElementCollection private Set<String> tags = new HashSet<>();

  public FinRecord(
      Journal journal,
      String name,
      String description,
      Instant happenedAt,
      boolean isContingent,
      Set<FinRecordItem> items,
      Set<String> tags) {
    this.journal = journal;
    this.name = name;
    this.description = description;
    this.happenedAt = happenedAt;
    this.isContingent = isContingent;
    this.items = new HashSet<>(items);
    this.tags = new HashSet<>(tags);
  }

  public void setItems(final Set<FinRecordItem> items) {
    this.items.clear();
    this.items.addAll(items);
  }

  public void setItemsWithInput(final Set<FinRecordItemInput> items) {
    this.items.clear();
    for (var item : items) {
      var account =
          journal.getAccounts().stream()
              .filter(a -> Objects.equals(a.getId(), item.accountId()))
              .findFirst();
      if (account.isEmpty()) {
        throw new CoreException.NotFound(Account.class, item.accountId());
      }
      this.items.add(new FinRecordItem(account.get(), item));
    }
  }

  public void setTags(Set<String> tags) {
    this.tags = new HashSet<>(tags);
  }

  @Override
  public PresentationModel toPresentationModel() {
    return null;
  }

  public record PresentationModel(UUID id) implements AbstractPresentationModel {}

  public void create(
      final Journal journal,
      final String name,
      final String description,
      final Instant happenedAt,
      final boolean isContingent,
      final Set<FinRecordItemInput> items,
      final Set<String> tags) {
    if (this.getId() != null) {
      throw new CoreException.AlreadyInitialized(this);
    }

    this.journal = journal;
    this.name = name;
    this.description = description;
    this.happenedAt = happenedAt;
    this.isContingent = isContingent;
    this.setItemsWithInput(items);
    this.setTags(tags);
  }

  public void update(
      @Nullable final String name,
      @Nullable final String description,
      @Nullable final Instant happenedAt,
      @Nullable final Boolean isContingent,
      @Nullable final Set<FinRecordItemInput> items,
      @Nullable final Set<String> tags) {
    if (items != null) {
      this.setItemsWithInput(items);
    }
  }
}
