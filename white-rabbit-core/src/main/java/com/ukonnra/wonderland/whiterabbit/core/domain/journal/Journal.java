package com.ukonnra.wonderland.whiterabbit.core.domain.journal;

import com.ukonnra.wonderland.whiterabbit.core.domain.journal.entity.Account;
import com.ukonnra.wonderland.whiterabbit.core.domain.journal.entity.FinRecord;
import com.ukonnra.wonderland.whiterabbit.core.domain.journal.valobj.FinRecordItemInput;
import com.ukonnra.wonderland.whiterabbit.core.domain.user.User;
import com.ukonnra.wonderland.whiterabbit.core.infrastructure.AbstractAggregateRoot;
import com.ukonnra.wonderland.whiterabbit.core.infrastructure.AbstractPresentationModel;
import com.ukonnra.wonderland.whiterabbit.core.infrastructure.CoreException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.persistence.CascadeType;
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
import org.springframework.lang.Nullable;

@Entity(name = "journals")
@Getter
@Setter
@ToString
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@SuppressWarnings("squid:S2160")
public final class Journal extends AbstractAggregateRoot<Journal.PresentationModel, JournalEvent> {
  @Column(nullable = false)
  private String name;

  @ManyToMany @ToString.Exclude Set<User> admins = new HashSet<>();

  @ManyToMany @ToString.Exclude Set<User> members = new HashSet<>();

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "journal")
  @ToString.Exclude
  Set<Account> accounts = new HashSet<>();

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "journal")
  @ToString.Exclude
  Set<FinRecord> finRecords = new HashSet<>();

  @Override
  public PresentationModel toPresentationModel() {
    return null;
  }

  public record PresentationModel(UUID id) implements AbstractPresentationModel {}

  public void setAdmins(Set<User> admins) {
    this.admins = new HashSet<>(admins);
  }

  public void setMembers(Set<User> members) {
    this.members = new HashSet<>(members);
  }

  public void setAccounts(Set<Account> accounts) {
    this.accounts = new HashSet<>(accounts);
  }

  public void setFinRecords(Set<FinRecord> finRecords) {
    this.finRecords = new HashSet<>(finRecords);
  }

  public void setName(String name) {
    var nameValue = name.trim();
    if (nameValue.isEmpty()) {
      throw new CoreException.FieldCouldNotBeEmpty(Journal.class, "name");
    }
    this.name = nameValue;
  }

  /**
   * Re-create the admins and members based on the inputs.
   *
   * <ul>
   *   <li>If a user existing in both admins and members, put it in admins
   *   <li>Otherwise, put it as-is
   * </ul>
   *
   * @param admins
   * @param members
   * @return
   */
  private void setAdminsAndMembers(
      @Nullable final Set<User> admins, @Nullable final Set<User> members) {

    var adminValues = new HashSet<User>();
    var memberValues = new HashSet<User>();

    if (members != null && admins != null) {
      for (var member : members) {
        if (admins.contains(member)) {
          adminValues.add(member);
        } else {
          memberValues.add(member);
        }
      }
    } else if (members != null) {
      memberValues.addAll(members);
    }

    if (admins != null) {
      adminValues.addAll(admins);
    }

    this.admins = adminValues;
    this.members = memberValues;
  }

  private enum AccessType {
    PUBLIC,
    MEMBERS_ADMINS,
    ADMINS_ONLY
  }

  private boolean isWriteable(final AccessType expectedAccessType, final User operator) {
    if (operator.getRole() != User.Role.USER) {
      return true;
    } else {
      return switch (expectedAccessType) {
        case PUBLIC -> true;
        case MEMBERS_ADMINS -> this.members.contains(operator) || this.admins.contains(operator);
        case ADMINS_ONLY -> this.admins.contains(operator);
      };
    }
  }

  private void checkBeforeUpdate(final AccessType expectedAccessType, final User operator) {
    if (!this.isWriteable(expectedAccessType, operator)) {
      throw new CoreException.OperatorNotWriteable(operator, this);
    }

    if (this.isDeleted()) {
      throw new CoreException.NotFound(this);
    }
  }

  public void create(
      final User operator,
      final String name,
      @Nullable final Set<User> admins,
      @Nullable final Set<User> members) {
    if (this.getId() != null) {
      throw new CoreException.AlreadyInitialized(this);
    }

    var adminValues = admins == null ? new HashSet<User>() : new HashSet<>(admins);
    adminValues.add(operator);

    this.setName(name);
    this.setAdminsAndMembers(adminValues, members);
  }

  public void update(
      final User operator,
      @Nullable final String name,
      @Nullable final Set<User> admins,
      @Nullable final Set<User> members) {
    if (name == null && admins == null && members == null) {
      throw new CoreException.EmptyUpdateOperation(this);
    }

    this.checkBeforeUpdate(AccessType.ADMINS_ONLY, operator);

    if (name != null) {
      this.setName(name);
    }

    var adminValues = admins == null ? this.admins : admins;
    var memberValues = members == null ? this.members : members;

    this.setAdminsAndMembers(adminValues, memberValues);
  }

  public void delete(final User operator) {
    this.checkBeforeUpdate(AccessType.ADMINS_ONLY, operator);

    this.setDeleted(true);
  }

  public void createAccount(
      final User operator,
      final String name,
      final String description,
      final Account.Type type,
      final String unit,
      final Account.InventoryType inventoryType) {
    this.checkBeforeUpdate(AccessType.MEMBERS_ADMINS, operator);

    var account = new Account();
    account.create(this, name, description, type, unit, inventoryType);
    this.accounts.add(account);
  }

  public void updateAccount(
      final User operator,
      final Account account,
      @Nullable final String name,
      @Nullable final String description,
      @Nullable final Account.Type type,
      @Nullable final String unit,
      @Nullable final Account.InventoryType inventoryType) {
    this.checkBeforeUpdate(AccessType.MEMBERS_ADMINS, operator);

    if (!account.getJournal().equals(this)) {
      throw new CoreException.NestedEntityNotMatch(this, account);
    }

    account.update(name, description, type, unit, inventoryType);
  }

  public void deleteAccount(final User operator, final Account account) {
    this.checkBeforeUpdate(AccessType.MEMBERS_ADMINS, operator);

    if (!account.getJournal().equals(this)) {
      throw new CoreException.NestedEntityNotMatch(this, account);
    }

    account.delete();
  }

  public void createFinRecord(
      final User operator,
      final String name,
      final String description,
      final Instant happenedAt,
      final boolean isContingent,
      final Set<FinRecordItemInput> items,
      final Set<String> tags) {
    this.checkBeforeUpdate(AccessType.MEMBERS_ADMINS, operator);

    var record = new FinRecord();
    record.create(this, name, description, happenedAt, isContingent, items, tags);
    this.finRecords.add(record);
  }

  public void updateFinRecord(
      final User operator,
      final FinRecord finRecord,
      @Nullable final String name,
      @Nullable final String description,
      @Nullable final Instant happenedAt,
      @Nullable final Boolean isContingent,
      @Nullable final Set<FinRecordItemInput> items,
      @Nullable final Set<String> tags) {
    this.checkBeforeUpdate(AccessType.MEMBERS_ADMINS, operator);

    if (!finRecord.getJournal().equals(this)) {
      throw new CoreException.NestedEntityNotMatch(this, finRecord);
    }

    finRecord.update(name, description, happenedAt, isContingent, items, tags);
  }
}
