package com.ukonnra.wonderland.whiterabbit.endpoint.graphql;

import com.ukonnra.wonderland.whiterabbit.core.CoreConfiguration;
import graphql.scalars.ExtendedScalars;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

@Configuration
@Import(CoreConfiguration.class)
public class ApplicationConfiguration {
  @Bean
  public RuntimeWiringConfigurer runtimeWiringConfigurer() {
    return wb -> wb.scalar(ExtendedScalars.DateTime).scalar(ExtendedScalars.GraphQLLong);
  }
}
