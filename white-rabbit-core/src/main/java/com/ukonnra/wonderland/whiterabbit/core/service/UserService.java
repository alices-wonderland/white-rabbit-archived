package com.ukonnra.wonderland.whiterabbit.core.service;

import com.ukonnra.wonderland.whiterabbit.core.entity.User;
import com.ukonnra.wonderland.whiterabbit.core.repository.UserRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public final class UserService {
  private final UserRepository repository;

  public UserService(UserRepository repository) {
    this.repository = repository;
  }

  public Mono<User> save(final User user) {
    return Mono.fromCallable(() -> this.repository.save(user))
        .publishOn(Schedulers.boundedElastic());
  }

  public Mono<User> findById(final UUID id) {
    return Mono.fromCallable(() -> this.repository.findById(id))
        .publishOn(Schedulers.boundedElastic())
        .flatMap(Mono::justOrEmpty);
  }
}
