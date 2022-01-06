package com.ukonnra.wonderland.whiterabbit.core.domain.journal;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.ukonnra.wonderland.whiterabbit.core.infrastructure.AbstractService;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
public class JournalService
    extends AbstractService<Journal, JournalRepository, Journal.PresentationModel> {
  protected JournalService(JournalRepository repository) {
    super(repository);
  }

  @Override
  public <V> V getProtectedFieldById(UserDetails user, UUID id, String field) {
    return null;
  }

  @Override
  public BooleanExpression createPaginationItemFilterByField(
      UUID id, String field, @Nullable Boolean isAfter) {
    return null;
  }

  @EventListener
  @Transactional
  public Mono<Void> handle(final JournalEvent.Deleted event) {
    return Mono.fromRunnable(() -> this.repository.deleteById(event.id()))
        .publishOn(Schedulers.boundedElastic())
        .doOnTerminate(() -> log.info("Receive event: {}", event))
        .then();
  }
}
