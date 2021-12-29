package com.ukonnra.wonderland.whiterabbit.core.domain.journal.enetity;

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

@Entity(name = "finRecordItems")
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class FinRecordItem extends AbstractEntity<FinRecordItem.PresentationModel> {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @ToString.Exclude
  private Account account;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @ToString.Exclude
  private FinRecord record;

  @Column(nullable = false)
  private @PositiveOrZero BigDecimal amount;

  private @Nullable String buyingUnit;

  private @Nullable BigDecimal buyingPrice;

  private @Nullable String note;

  @Override
  public PresentationModel toPresentationModel() {
    return null;
  }

  public record PresentationModel() implements AbstractPresentationModel {

    @Override
    public UUID id() {
      return null;
    }
  }
}
