package com.ukonnra.wonderland.whiterabbit.core.infrastructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.persistence.MappedSuperclass;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;
import org.springframework.util.Assert;

@MappedSuperclass
@SuppressWarnings("squid:S2160")
public abstract class AbstractAggregateRoot<P extends AbstractPresentationModel, E>
    extends AbstractEntity<P> {
  private final transient @Transient List<E> domainEvents = new ArrayList<>();

  protected E registerEvent(E event) {

    Assert.notNull(event, "Domain event must not be null!");

    this.domainEvents.add(event);
    return event;
  }

  @AfterDomainEventPublication
  protected void clearDomainEvents() {
    this.domainEvents.clear();
  }

  @DomainEvents
  protected Collection<E> domainEvents() {
    return Collections.unmodifiableList(domainEvents);
  }
}
