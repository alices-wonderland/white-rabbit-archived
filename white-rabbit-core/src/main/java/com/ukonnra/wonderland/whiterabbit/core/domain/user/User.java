package com.ukonnra.wonderland.whiterabbit.core.domain.user;

import com.ukonnra.wonderland.whiterabbit.core.infrastructure.AbstractAggregateRoot;
import com.ukonnra.wonderland.whiterabbit.core.infrastructure.AbstractPresentationModel;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity(name = "users")
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public final class User extends AbstractAggregateRoot<User.PresentationModel, UserEvent> {
  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private Role role;

  @Override
  public PresentationModel toPresentationModel() {
    return null;
  }

  public enum Role {
    USER,
    ADMIN,
    OWNER
  }

  public record PresentationModel(UUID id, String name, Role role)
      implements AbstractPresentationModel {}
}
