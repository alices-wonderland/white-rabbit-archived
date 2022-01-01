package com.ukonnra.wonderland.whiterabbit.core.infrastructure;

import com.ukonnra.wonderland.whiterabbit.core.domain.user.valobj.Identifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = MockJwtSecurityContextFactory.class)
public @interface WithJwt {
  Identifier.Type type() default Identifier.Type.AUTHING;

  String id();
}
