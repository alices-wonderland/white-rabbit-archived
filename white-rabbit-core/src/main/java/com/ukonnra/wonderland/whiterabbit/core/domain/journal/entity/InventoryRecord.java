package com.ukonnra.wonderland.whiterabbit.core.domain.journal.entity;

import com.ukonnra.wonderland.whiterabbit.core.infrastructure.AbstractEntity;
import com.ukonnra.wonderland.whiterabbit.core.infrastructure.AbstractPresentationModel;
import java.math.BigDecimal;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.lang.Nullable;

@Entity(name = "inventoryRecords")
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@SuppressWarnings("squid:S2160")
public final class InventoryRecord extends AbstractEntity<FinRecord.PresentationModel> {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @ToString.Exclude
  private Inventory inventory;

  private @Column(nullable = false) @PositiveOrZero BigDecimal amount;

  private @Nullable String buyingUnit;

  private @Nullable BigDecimal buyingPrice;

  @Override
  public FinRecord.PresentationModel toPresentationModel() {
    return null;
  }

  record PresentationModel() implements AbstractPresentationModel {
    @Override
    public UUID id() {
      return null;
    }
  }
}
