package com.ukonnra.wonderland.whiterabbit.core.infrastructure;

import com.ukonnra.wonderland.whiterabbit.core.domain.user.User;
import java.util.UUID;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.jpa.domain.AbstractAuditable;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@MappedSuperclass
@SuppressWarnings("squid:S2160")
public abstract class AbstractEntity<P extends AbstractPresentationModel>
    extends AbstractAuditable<User, UUID> {
  @Version private Long version;

  public abstract P toPresentationModel();
}
