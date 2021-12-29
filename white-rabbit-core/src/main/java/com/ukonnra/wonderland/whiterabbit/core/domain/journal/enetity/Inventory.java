package com.ukonnra.wonderland.whiterabbit.core.domain.journal.enetity;

import com.ukonnra.wonderland.whiterabbit.core.infrastructure.AbstractEntity;
import com.ukonnra.wonderland.whiterabbit.core.infrastructure.AbstractPresentationModel;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity(name = "inventories")
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn
// Cannot use sealed class, since JDK proxy need to inherit it too
public abstract class Inventory extends AbstractEntity<Inventory.PresentationModel> {
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @ToString.Exclude
  private Account account;

  @Entity(name = "inventories_average")
  @Getter
  @Setter
  @ToString(callSuper = true)
  @NoArgsConstructor
  @EntityListeners(AuditingEntityListener.class)
  @DiscriminatorValue("AVERAGE")
  public static final class Average extends Inventory {
    @OneToOne(
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY,
        optional = false,
        mappedBy = "inventory",
        orphanRemoval = true)
    private InventoryRecord record;

    @Override
    public PresentationModel toPresentationModel() {
      return null;
    }
  }

  @Entity(name = "inventories_fifo")
  @Getter
  @Setter
  @ToString(callSuper = true)
  @NoArgsConstructor
  @EntityListeners(AuditingEntityListener.class)
  @DiscriminatorValue("FIFO")
  public static final class Fifo extends Inventory {
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "inventory", orphanRemoval = true)
    private Set<InventoryRecord> records = new HashSet<>();

    @Override
    public PresentationModel toPresentationModel() {
      return null;
    }
  }

  public sealed interface PresentationModel extends AbstractPresentationModel
      permits PresentationModel.Fifo, PresentationModel.Average {
    record Average(UUID id, UUID record) implements PresentationModel {}

    record Fifo(UUID id, Set<UUID> records) implements PresentationModel {}
  }
}
