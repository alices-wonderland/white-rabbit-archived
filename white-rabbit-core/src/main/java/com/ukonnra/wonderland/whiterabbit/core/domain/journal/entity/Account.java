package com.ukonnra.wonderland.whiterabbit.core.domain.journal.entity;

import com.ukonnra.wonderland.whiterabbit.core.domain.journal.Journal;
import com.ukonnra.wonderland.whiterabbit.core.infrastructure.AbstractEntity;
import com.ukonnra.wonderland.whiterabbit.core.infrastructure.AbstractPresentationModel;
import com.ukonnra.wonderland.whiterabbit.core.infrastructure.CoreException;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.lang.Nullable;

@Entity(name = "accounts")
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@SuppressWarnings("squid:S2160")
public final class Account extends AbstractEntity<Account.PresentationModel> {
  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String description;

  @Column(nullable = false)
  private Type type;

  @Column(nullable = false)
  private String unit;

  @Column(nullable = false)
  private InventoryType inventoryType;

  @ManyToOne(optional = false)
  private Journal journal;

  public Account(
      String name,
      String description,
      Type type,
      String unit,
      InventoryType inventoryType,
      Journal journal) {
    this.name = name;
    this.description = description;
    this.type = type;
    this.unit = unit;
    this.inventoryType = inventoryType;
    this.journal = journal;
  }

  @Override
  public PresentationModel toPresentationModel() {
    return null;
  }

  public record PresentationModel(UUID id) implements AbstractPresentationModel {}

  public enum Type {
    ASSET,
    LIABILITY,
    INCOME,
    EXPENSE,
    EQUITY
  }

  public enum InventoryType {
    FIFO,
    AVERAGE
  }

  public void setName(String name) {
    var nameValue = name.trim();
    if (nameValue.isEmpty()) {
      throw new CoreException.FieldCouldNotBeEmpty(Account.class, "name");
    }
    this.name = nameValue;
  }

  public void setDescription(String description) {
    this.description = description.trim();
  }

  public void create(
      final Journal journal,
      final String name,
      final String description,
      final Account.Type type,
      final String unit,
      final InventoryType inventoryType) {
    if (this.getId() != null) {
      throw new CoreException.AlreadyInitialized(this);
    }

    this.setName(name);
    this.setDescription(description.trim());
    this.setType(type);
    this.setUnit(unit);
    this.setInventoryType(inventoryType);
    this.setJournal(journal);
  }

  public void update(
      @Nullable final String name,
      @Nullable final String description,
      @Nullable final Account.Type type,
      @Nullable final String unit,
      @Nullable final InventoryType inventoryType) {
    if (this.isDeleted()) {
      throw new CoreException.NotFound(this);
    }

    if (name == null
        && description == null
        && type == null
        && unit == null
        && inventoryType == null) {
      throw new CoreException.EmptyUpdateOperation(this);
    }

    if (name != null) {
      this.setName(name);
    }

    if (description != null) {
      this.setDescription(description);
    }

    if (type != null) {
      this.setType(type);
    }

    if (unit != null) {
      this.setUnit(unit);
    }

    if (inventoryType != null) {
      this.setInventoryType(inventoryType);
    }
  }

  public void delete() {
    if (this.isDeleted()) {
      throw new CoreException.NotFound(this);
    }

    this.setDeleted(true);
  }
}
