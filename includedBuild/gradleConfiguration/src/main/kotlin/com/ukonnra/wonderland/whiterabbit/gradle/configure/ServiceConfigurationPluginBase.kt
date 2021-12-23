package com.ukonnra.wonderland.whiterabbit.gradle.configure

import io.freefair.gradle.plugins.lombok.LombokPlugin
import io.spring.gradle.dependencymanagement.DependencyManagementPlugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.exclude
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.named
import org.springframework.boot.gradle.plugin.SpringBootPlugin

abstract class ServiceConfigurationPluginBase : ConfigurationPluginBase() {

  abstract fun doApply(target: Project)

  override fun apply(target: Project) {
    super.apply(target)

    doApply(target)

    target.apply<LombokPlugin>()
    target.apply<DependencyManagementPlugin>()

    target.configurations.apply {
      named<Configuration>("compileOnly") {
        extendsFrom(target.configurations.getByName<Configuration>("annotationProcessor"))
      }

      named<Configuration>("implementation") {
        exclude(module = "spring-boot-starter-tomcat")
      }
    }

    target.dependencies {
      "implementation"(platform("org.testcontainers:testcontainers-bom:1.16.2"))
      "implementation"(platform(SpringBootPlugin.BOM_COORDINATES))

      "testImplementation"("org.springframework.boot:spring-boot-starter-test")
      "testImplementation"("io.projectreactor:reactor-test")
      "testImplementation"("org.testcontainers:junit-jupiter")
      "testImplementation"("org.testcontainers:postgresql")
    }
  }
}
