module whiterabbit.core {
  exports com.ukonnra.wonderland.whiterabbit.core;
  exports com.ukonnra.wonderland.whiterabbit.core.auth;
  exports com.ukonnra.wonderland.whiterabbit.core.domain.journal;
  exports com.ukonnra.wonderland.whiterabbit.core.domain.journal.enetity;
  exports com.ukonnra.wonderland.whiterabbit.core.domain.user;
  exports com.ukonnra.wonderland.whiterabbit.core.infrastructure;
  exports com.ukonnra.wonderland.whiterabbit.core.infrastructure.query;

  opens com.ukonnra.wonderland.whiterabbit.core;
  opens com.ukonnra.wonderland.whiterabbit.core.domain.journal;
  opens com.ukonnra.wonderland.whiterabbit.core.domain.journal.enetity;
  opens com.ukonnra.wonderland.whiterabbit.core.domain.user;
  opens com.ukonnra.wonderland.whiterabbit.core.infrastructure;
  opens com.ukonnra.wonderland.whiterabbit.core.infrastructure.query;

  requires java.compiler;
  requires java.persistence;
  requires java.validation;
  requires com.querydsl.core;
  requires lombok;
  requires org.hibernate.orm.core;
  requires reactor.core;
  requires spring.boot.autoconfigure;
  requires spring.context;
  requires spring.core;
  requires spring.data.commons;
  requires spring.data.jpa;
  requires spring.security.core;
  requires spring.security.config;
  requires spring.security.web;
  requires spring.tx;
}
