package com.ukonnra.wonderland.whiterabbit.core.service.impl;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserDetailsServiceImpl implements ReactiveUserDetailsService {
  @Override
  public Mono<UserDetails> findByUsername(String username) {
    return Mono.empty();
  }
}
