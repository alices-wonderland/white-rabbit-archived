package com.ukonnra.wonderland.whiterabbit.core.domain.journal.entity;

import com.ukonnra.wonderland.whiterabbit.core.domain.journal.Journal;
import com.ukonnra.wonderland.whiterabbit.core.infrastructure.AbstractEntity;
import com.ukonnra.wonderland.whiterabbit.core.infrastructure.AbstractPresentationModel;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity(name = "accounts")
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@SuppressWarnings("squid:S2160")
public final class Account extends AbstractEntity<Account.PresentationModel> {
  @Column(nullable = false)
  private Type type;

  @Column(nullable = false)
  private String unit;

  @OneToOne(cascade = CascadeType.ALL, optional = false, orphanRemoval = true, mappedBy = "account")
  private Inventory inventory;

  @ManyToOne(optional = false)
  private Journal journal;

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
}
