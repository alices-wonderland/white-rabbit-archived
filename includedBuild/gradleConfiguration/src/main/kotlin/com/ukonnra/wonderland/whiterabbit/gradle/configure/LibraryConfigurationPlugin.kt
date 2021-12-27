package com.ukonnra.wonderland.whiterabbit.gradle.configure

import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.Publication
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPomLicenseSpec
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

open class LibraryConfigurationPlugin : ServiceConfigurationPluginBase() {
  override fun doApply(target: Project) {
    target.apply<MavenPublishPlugin>()
    target.apply<JavaLibraryPlugin>()

    target.configure<JavaPluginExtension> {
      withSourcesJar()
    }

    target.configure<PublishingExtension> {
      publications.registerKt<MavenPublication>("release") {
        from(target.components["java"])
        pomKt {
          licenseKt {
            license {
              name.set("The Apache License, Version 2.0")
              url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
          }
        }
      }
    }

    target.tasks.named<Jar>(JavaPlugin.JAR_TASK_NAME) {
      enabled = true
    }
  }
}

private inline fun <reified T : Publication> PublicationContainer.registerKt(
  name: String,
  noinline configuration: T.() -> Unit
): NamedDomainObjectProvider<T> = this.register(name, configuration)

private fun MavenPublication.pomKt(configuration: MavenPom.() -> Unit): Unit =
  this.pom(configuration)

private fun MavenPom.licenseKt(configuration: MavenPomLicenseSpec.() -> Unit): Unit =
  this.licenses(configuration)
