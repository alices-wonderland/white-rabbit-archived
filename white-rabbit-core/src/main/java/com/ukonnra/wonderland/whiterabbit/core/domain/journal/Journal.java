package com.ukonnra.wonderland.whiterabbit.core.domain.journal;

import com.ukonnra.wonderland.whiterabbit.core.domain.journal.enetity.Account;
import com.ukonnra.wonderland.whiterabbit.core.domain.journal.enetity.FinRecord;
import com.ukonnra.wonderland.whiterabbit.core.domain.user.User;
import com.ukonnra.wonderland.whiterabbit.core.infrastructure.AbstractAggregateRoot;
import com.ukonnra.wonderland.whiterabbit.core.infrastructure.AbstractPresentationModel;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity(name = "journals")
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public final class Journal extends AbstractAggregateRoot<Journal.PresentationModel, JournalEvent> {
  @Column(nullable = false)
  private String name;

  @ManyToMany @ToString.Exclude Set<User> admins = new HashSet<>();

  @ManyToMany @ToString.Exclude Set<User> members = new HashSet<>();

  @OneToMany(mappedBy = "journal")
  @ToString.Exclude
  Set<Account> accounts = new HashSet<>();

  @OneToMany(mappedBy = "journal")
  @ToString.Exclude
  Set<FinRecord> finRecords = new HashSet<>();

  @Override
  public PresentationModel toPresentationModel() {
    return null;
  }

  public record PresentationModel(UUID id) implements AbstractPresentationModel {}
}
