package com.ukonnra.wonderland.whiterabbit.gradle.configure

import com.palantir.gradle.docker.DockerExtension
import com.palantir.gradle.docker.PalantirDockerPlugin
import org.gradle.api.Project
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

    target.tasks.named<BootBuildImage>("bootBuildImage") {
      val imageTag = System.getenv("IMAGE_TAG") ?: "latest"
      imageName = "ukonnra/${target.name}:$imageTag"

      val proxyUrl = System.getenv("BUILD_IMAGE_PROXY")
      if (!proxyUrl.isNullOrEmpty()) {
        environment = mapOf(
          "HTTP_PROXY" to proxyUrl,
          "HTTPS_PROXY" to proxyUrl
        )
      }
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
