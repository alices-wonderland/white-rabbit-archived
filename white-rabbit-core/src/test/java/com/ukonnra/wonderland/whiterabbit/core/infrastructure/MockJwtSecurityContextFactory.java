package com.ukonnra.wonderland.whiterabbit.core.infrastructure;

import java.util.UUID;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public final class MockJwtSecurityContextFactory implements WithSecurityContextFactory<WithJwt> {
  @Override
  public SecurityContext createSecurityContext(WithJwt annotation) {
    var context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(
        new JwtAuthenticationToken(
            Jwt.withTokenValue(UUID.randomUUID().toString())
                .issuer(annotation.type().getIssuer())
                .subject(annotation.id())
                .header("header", "header-value")
                .build()));
    return context;
  }
}
