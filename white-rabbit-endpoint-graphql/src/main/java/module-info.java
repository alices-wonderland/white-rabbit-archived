module whiterabbit.endpoint.graphql {
  exports com.ukonnra.wonderland.whiterabbit.endpoint.graphql.controller to
      spring.graphql;

  opens com.ukonnra.wonderland.whiterabbit.endpoint.graphql to
      spring.beans,
      spring.context,
      spring.core;
  opens com.ukonnra.wonderland.whiterabbit.endpoint.graphql.controller to
      spring.beans,
      spring.core;

  requires transitive whiterabbit.core;
  requires java.sql;
  requires java.transaction;
  requires com.graphqljava;
  requires graphql.java.extended.scalars;
  requires lombok;
  requires org.slf4j;
  requires reactor.core;
  requires spring.aop;
  requires spring.beans;
  requires spring.boot;
  requires spring.boot.autoconfigure;
  requires spring.context;
  requires spring.core;
  requires spring.data.commons;
  requires spring.graphql;
}
