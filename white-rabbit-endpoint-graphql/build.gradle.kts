plugins {
  id("application-configuration")
}

dependencies {
  implementation(project(":white-rabbit-core"))
  implementation("org.springframework.experimental:graphql-spring-boot-starter:1.0.0-M4")
  implementation("com.graphql-java:graphql-java-extended-scalars:17.0")
  implementation("org.springframework.boot:spring-boot-starter-websocket")
  implementation("com.graphql-java:java-dataloader:3.1.1")
}

application {
  mainClass.set("com.ukonnra.wonderland.whiterabbit.endpoint.graphql.Application")
}
