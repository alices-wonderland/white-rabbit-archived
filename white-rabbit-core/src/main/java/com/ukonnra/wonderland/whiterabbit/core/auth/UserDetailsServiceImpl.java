package com.ukonnra.wonderland.whiterabbit.core.auth;

import com.ukonnra.wonderland.whiterabbit.core.domain.user.UserService;
import java.util.UUID;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserDetailsServiceImpl implements ReactiveUserDetailsService {
  private final UserService userService;

  public UserDetailsServiceImpl(UserService userService) {
    this.userService = userService;
  }

  @Override
  public Mono<UserDetails> findByUsername(String username) {
    return this.userService
        .findById(UUID.fromString(username))
        .map(u -> User.withUsername(username).roles(u.role().name()).build());
  }
}
