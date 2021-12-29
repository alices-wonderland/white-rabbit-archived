package com.ukonnra.wonderland.whiterabbit.core.domain.journal.enetity;

import com.ukonnra.wonderland.whiterabbit.core.domain.journal.Journal;
import com.ukonnra.wonderland.whiterabbit.core.domain.user.User;
import com.ukonnra.wonderland.whiterabbit.core.infrastructure.AbstractEntity;
import com.ukonnra.wonderland.whiterabbit.core.infrastructure.AbstractPresentationModel;
import java.time.Instant;
import java.util.HashSet;
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

@Entity(name = "finRecords")
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public final class FinRecord extends AbstractEntity<FinRecord.PresentationModel> {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @ToString.Exclude
  private Journal journal;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @ToString.Exclude
  private User author;

  @Column(nullable = false)
  private Instant happenedAt;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private boolean isContingent = false;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "record", orphanRemoval = true)
  @ToString.Exclude
  private Set<FinRecordItem> items = new HashSet<>();

  @ElementCollection private Set<String> tags = new HashSet<>();

  @Column(nullable = false)
  private String basicUnit;

  private String note;

  @Override
  public PresentationModel toPresentationModel() {
    return null;
  }

  public record PresentationModel(UUID id) implements AbstractPresentationModel {}
}
