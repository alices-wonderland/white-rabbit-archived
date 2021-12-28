package com.ukonnra.wonderland.whiterabbit.gradle.configure

import com.diffplug.gradle.spotless.JavaExtension
import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin
import com.github.benmanes.gradle.versions.VersionsPlugin
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.github.spotbugs.snom.SpotBugsExtension
import com.github.spotbugs.snom.SpotBugsPlugin
import com.github.spotbugs.snom.SpotBugsReport
import com.github.spotbugs.snom.SpotBugsTask
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.plugins.quality.CheckstylePlugin
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.closureOf
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.sonarqube.gradle.SonarQubeExtension
import org.sonarqube.gradle.SonarQubePlugin
import org.sonarqube.gradle.SonarQubeProperties
import java.net.URI

abstract class ConfigurationPluginBase : Plugin<Project> {
  companion object {
    internal val JAVA_VERSION = JavaVersion.VERSION_17
  }

  override fun apply(target: Project) {
    target.apply<IdeaPlugin>()
    target.apply<JavaPlugin>()
    target.apply<JacocoPlugin>()
    target.apply<VersionsPlugin>()
    target.apply<SpotlessPlugin>()
    target.apply<CheckstylePlugin>()
    target.apply<SpotBugsPlugin>()
    target.apply<SonarQubePlugin>()

    target.group = "com.ukonnra.wonderland"

    target.repositories.apply {
      mavenCentral()
      for (dist in listOf("release", "milestone")) {
        maven(
          closureOf<MavenArtifactRepository> {
            url = URI.create("https://repo.spring.io/$dist")
          }
        )
      }
    }

    target.configure<JavaPluginExtension> {
      sourceCompatibility = JAVA_VERSION
      targetCompatibility = JAVA_VERSION
      modularity.inferModulePath.set(false)
    }

    target.configure<JacocoPluginExtension> {
      toolVersion = "0.8.7"
    }

    target.configure<SpotBugsExtension> {
      excludeFilter.set(target.file("${target.rootDir}/config/spotbugs/exclude.xml"))
      includeFilter.set(target.file("${target.rootDir}/config/spotbugs/include.xml"))
    }

    target.configure<CheckstyleExtension> {
      val sourceSetsNeeded = target.extensions.getByType<SourceSetContainer>().let {
        listOf(it["main"], it["test"])
      }

      sourceSets = sourceSetsNeeded
      toolVersion = "9.2"
    }

    target.configure<SpotlessExtension> {
      javaKt {
        importOrder()
        removeUnusedImports()
        googleJavaFormat()
        indentWithSpaces(2)
        target("src/*/java/**/*.java")
      }
    }

    target.configure<SonarQubeExtension> {
      propertiesKt {
        property("sonar.projectKey", "alices-wonderland_white-rabbit")
        property("sonar.organization", "alices-wonderland")
        property("sonar.host.url", "https://sonarcloud.io")
      }
    }

    target.tasks.configureTasks()
  }

  private fun TaskContainer.configureTasks() {
    named<SpotBugsTask>("spotbugsMain") {
      for (report in listOf("xml", "html")) {
        reports.create(
          report,
          closureOf<SpotBugsReport> {
            required.set(true)
          }
        )
      }
    }

    named<Test>(JavaPlugin.TEST_TASK_NAME) {
      finalizedBy(named("jacocoTestReport"))
      useJUnitPlatform()
      testLogging.apply {
        events("passed", "skipped", "failed")
        exceptionFormat = TestExceptionFormat.FULL
        showStackTraces = true
      }
    }

    withType<Delete> {
      delete("out", "logs")
    }

    withType<DependencyUpdatesTask> {
      checkForGradleUpdate = false
    }
  }
}

private fun SpotlessExtension.javaKt(configuration: JavaExtension.() -> Unit): Unit =
  this.java(configuration)

private fun SonarQubeExtension.propertiesKt(properties: SonarQubeProperties.() -> Unit): Unit =
  this.properties(properties)
