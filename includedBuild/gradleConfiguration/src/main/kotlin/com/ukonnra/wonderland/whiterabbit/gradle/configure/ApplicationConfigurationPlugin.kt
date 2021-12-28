package com.ukonnra.wonderland.whiterabbit.gradle.configure

import com.github.jengelman.gradle.plugins.shadow.ShadowJavaPlugin
import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.PropertiesFileTransformer
import com.palantir.gradle.docker.DockerExtension
import com.palantir.gradle.docker.PalantirDockerPlugin
import org.gradle.api.Project
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.named
import org.springframework.boot.gradle.plugin.SpringBootPlugin
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

class ApplicationConfigurationPlugin : ServiceConfigurationPluginBase() {
  override fun doApply(target: Project) {
    target.apply<SpringBootPlugin>()
    target.apply<PalantirDockerPlugin>()
    target.apply<ShadowPlugin>()
    target.apply<ApplicationPlugin>()

    // Q: Why now using the SpringBoot to build Jar?
    // A: The Jar created by SpringBoot cannot be analyzed via jdeps
    // Related: https://stackoverflow.com/questions/64066952/jdeps-module-not-found-exception-when-listing-dependancies
    target.tasks.named<BootBuildImage>("bootBuildImage") {
      enabled = false
    }

    // Using shadow plugin rather than the SpringBoot one to build Jar
    // How to use shadow on SpringBoot project: https://suspendfun.com/2020/Shadow-gradle-plugin-to-create-fat-jar/
    target.tasks.named<ShadowJar>(ShadowJavaPlugin.SHADOW_JAR_TASK_NAME) {
      isZip64 = true
      // Required for Spring
      mergeServiceFiles()
      append("META-INF/spring.handlers")
      append("META-INF/spring.schemas")
      append("META-INF/spring.tooling")
      transform(
        PropertiesFileTransformer().apply {
          paths = listOf("META-INF/spring.factories")
          mergeStrategy = "append"
        }
      )
    }

    target.configure<DockerExtension> {
      val imageTag = System.getenv("IMAGE_TAG") ?: "latest"
      name = "ukonnra/${target.name}:$imageTag"
      setDockerfile(target.file("Dockerfile"))
      files("${target.project.projectDir}/build/libs")
      pull(true)
      noCache(true)
    }

    target.dependencies {
      "implementation"("org.springframework.boot:spring-boot-starter-webflux")
      "implementation"("org.springframework.session:spring-session-core")
      "implementation"("org.springframework.boot:spring-boot-starter-actuator")

      "annotationProcessor"("org.springframework.boot:spring-boot-configuration-processor")

      "testImplementation"("org.springframework.boot:spring-boot-starter-test")
      "testImplementation"("io.projectreactor:reactor-test")
      "testImplementation"("org.testcontainers:junit-jupiter")
      "testImplementation"("org.testcontainers:postgresql")
    }
  }
}
