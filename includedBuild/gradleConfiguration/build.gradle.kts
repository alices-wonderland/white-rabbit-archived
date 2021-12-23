plugins {
  `kotlin-dsl`
  `java-gradle-plugin`

  id("com.github.ben-manes.versions") version "0.39.0"
  id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
  id("io.gitlab.arturbosch.detekt") version "1.19.0"
}

repositories {
  gradlePluginPortal()
  maven { url = uri("https://repo.spring.io/release") }
  maven { url = uri("https://repo.spring.io/milestone") }
}

dependencies {
  implementation("com.github.ben-manes:gradle-versions-plugin:0.39.0")
  implementation("com.diffplug.spotless:spotless-plugin-gradle:6.0.5")
  implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:5.0.3")
  implementation("org.javamodularity:moduleplugin:1.8.10")
  implementation("io.freefair.gradle:lombok-plugin:6.3.0")
  implementation("org.springframework.boot:spring-boot-gradle-plugin:2.6.2")
  implementation("io.spring.gradle:dependency-management-plugin:1.0.11.RELEASE")
//  implementation("org.springframework.experimental:spring-aot-gradle-plugin:0.11.1")
  implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:3.3")
}

gradlePlugin {
  plugins.register("project-configuration") {
    id = "project-configuration"
    implementationClass = "com.ukonnra.wonderland.whiterabbit.gradle.configure.ProjectConfigurationPlugin"
  }

  plugins.register("library-configuration") {
    id = "library-configuration"
    implementationClass = "com.ukonnra.wonderland.whiterabbit.gradle.configure.LibraryConfigurationPlugin"
  }

  plugins.register("application-configuration") {
    id = "application-configuration"
    implementationClass = "com.ukonnra.wonderland.whiterabbit.gradle.configure.ApplicationConfigurationPlugin"
  }
}

ktlint {
  version.set("0.43.2")
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

tasks.compileKotlin {
  kotlinOptions {
    jvmTarget = JavaVersion.VERSION_17.toString()
  }
}
