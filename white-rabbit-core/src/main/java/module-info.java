module whiterabbit.core {
  exports com.ukonnra.wonderland.whiterabbit.core;
  exports com.ukonnra.wonderland.whiterabbit.core.query;
  exports com.ukonnra.wonderland.whiterabbit.core.entity;
  exports com.ukonnra.wonderland.whiterabbit.core.service;

  requires lombok;
  requires spring.boot.autoconfigure;
  requires spring.context;
  requires spring.data.jpa;
  requires spring.tx;
  requires com.querydsl.core;
  requires spring.data.commons;
  requires spring.core;
  requires reactor.core;
  requires java.validation;
  requires java.persistence;
  requires java.compiler;
}
