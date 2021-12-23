package com.ukonnra.wonderland.whiterabbit.core.entity;

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
public abstract class AbstractEntity extends AbstractAuditable<User, UUID> {
  @Version private Long version;
}
