open module whiterabbit.core {
  requires spring.boot.autoconfigure;
  requires lombok;
  requires java.compiler;

  exports com.ukonnra.wonderland.whiterabbit.core;
  exports com.ukonnra.wonderland.whiterabbit.core.entity;
  exports com.ukonnra.wonderland.whiterabbit.core.query;
  exports com.ukonnra.wonderland.whiterabbit.core.service;
}
