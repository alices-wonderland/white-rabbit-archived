package com.ukonnra.wonderland.whiterabbit.core.auth;

import com.ukonnra.wonderland.whiterabbit.core.domain.user.QUser;
import com.ukonnra.wonderland.whiterabbit.core.domain.user.User;
import com.ukonnra.wonderland.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.wonderland.whiterabbit.core.domain.user.valobj.Identifier;
import com.ukonnra.wonderland.whiterabbit.core.infrastructure.CoreException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class UserDetailsServiceImpl implements ReactiveUserDetailsService {
  private final UserRepository userRepository;

  public UserDetailsServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Mono<UserDetails> findByUsername(String username) {
    return ReactiveSecurityContextHolder.getContext()
        .flatMap(
            ctx -> {
              if (ctx.getAuthentication() instanceof JwtAuthenticationToken jwt) {
                return Mono.fromCallable(
                        () ->
                            this.userRepository.findOne(
                                QUser.user.identifiers.contains(new Identifier(jwt.getToken()))))
                    .flatMap(Mono::justOrEmpty)
                    .filter(u -> u.getId() != null)
                    .map(User::toPresentationModel);
              } else {
                return Mono.error(new CoreException.InvalidToken());
              }
            });
  }
}
