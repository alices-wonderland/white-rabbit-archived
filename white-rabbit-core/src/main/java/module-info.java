module whiterabbit.core {
  exports com.ukonnra.wonderland.whiterabbit.core;
  exports com.ukonnra.wonderland.whiterabbit.core.domain.journal;
  exports com.ukonnra.wonderland.whiterabbit.core.domain.journal.entity;
  exports com.ukonnra.wonderland.whiterabbit.core.domain.journal.valobj;
  exports com.ukonnra.wonderland.whiterabbit.core.domain.user;
  exports com.ukonnra.wonderland.whiterabbit.core.domain.user.valobj;
  exports com.ukonnra.wonderland.whiterabbit.core.infrastructure;
  exports com.ukonnra.wonderland.whiterabbit.core.infrastructure.query;

  opens com.ukonnra.wonderland.whiterabbit.core;
  opens com.ukonnra.wonderland.whiterabbit.core.domain.journal;
  opens com.ukonnra.wonderland.whiterabbit.core.domain.journal.entity;
  opens com.ukonnra.wonderland.whiterabbit.core.domain.journal.valobj;
  opens com.ukonnra.wonderland.whiterabbit.core.domain.user;
  opens com.ukonnra.wonderland.whiterabbit.core.domain.user.valobj;
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
  requires spring.security.oauth2.core;
  requires spring.security.oauth2.resource.server;
  requires spring.security.web;
  requires spring.tx;
  requires spring.security.oauth2.jose;
}
