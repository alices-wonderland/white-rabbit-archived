module whiterabbit.core {
  exports com.ukonnra.wonderland.whiterabbit.core;
  exports com.ukonnra.wonderland.whiterabbit.core.command;
  exports com.ukonnra.wonderland.whiterabbit.core.entity;
  exports com.ukonnra.wonderland.whiterabbit.core.query;
  exports com.ukonnra.wonderland.whiterabbit.core.service;

  opens com.ukonnra.wonderland.whiterabbit.core to
      spring.beans,
      spring.context,
      spring.core;
  opens com.ukonnra.wonderland.whiterabbit.core.entity;
  opens com.ukonnra.wonderland.whiterabbit.core.query;
  opens com.ukonnra.wonderland.whiterabbit.core.service to
      spring.beans,
      spring.core;

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
  requires spring.tx;
  requires spring.security.config;
}
