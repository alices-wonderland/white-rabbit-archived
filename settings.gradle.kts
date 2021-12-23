rootProject.name = "white-rabbit"

pluginManagement {
  repositories {
    gradlePluginPortal()
    maven { url = uri("https://repo.spring.io/release") }
    maven { url = uri("https://repo.spring.io/milestone") }
  }
}

include(
  ":white-rabbit-core",
  ":white-rabbit-endpoint-graphql",
)

includeBuild("includedBuild/gradleConfiguration")
