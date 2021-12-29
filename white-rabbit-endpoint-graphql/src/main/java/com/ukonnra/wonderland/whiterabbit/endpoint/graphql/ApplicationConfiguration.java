package com.ukonnra.wonderland.whiterabbit.endpoint.graphql;

import com.ukonnra.wonderland.whiterabbit.core.CoreConfiguration;
import graphql.scalars.ExtendedScalars;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@Import(CoreConfiguration.class)
public class ApplicationConfiguration {
  @Bean
  public RuntimeWiringConfigurer runtimeWiringConfigurer() {
    return wb -> wb.scalar(ExtendedScalars.DateTime).scalar(ExtendedScalars.GraphQLLong);
  }

  @Bean
  SecurityWebFilterChain configure(ServerHttpSecurity http) {
    http.authorizeExchange(
            exchanges ->
                exchanges
                    .pathMatchers("/graphiql")
                    .permitAll()
                    .anyExchange()
                    .authenticated()
                    .and()
                    .cors(ServerHttpSecurity.CorsSpec::disable)
                    .csrf(ServerHttpSecurity.CsrfSpec::disable))
        .oauth2ResourceServer(server -> server.jwt(Customizer.withDefaults()));
    return http.build();
  }
}
