package com.ukonnra.wonderland.whiterabbit.gradle.configure

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.gradle.testing.jacoco.tasks.JacocoReportsContainer

class ProjectConfigurationPlugin : ConfigurationPluginBase() {
  override fun apply(target: Project) {
    super.apply(target)

    val codeCoverageReport = target.tasks.register<JacocoReport>("codeCoverageReport") {
      target.subprojects.map { it.tasks.named(JavaPlugin.TEST_TASK_NAME) }.forEach {
        dependsOn(it)
      }

      executionData(target.fileTree(project.rootDir.absolutePath).include("**/build/jacoco/*.exec"))

      target.subprojects.forEach {
        sourceSets((it.extensions.getByName("sourceSets") as SourceSetContainer)["main"])
      }

      reportsKt {
        xml.required.set(true)
        xml.outputLocation.set(target.file("${target.buildDir}/reports/jacoco/report.xml"))
        html.required.set(true)
        csv.required.set(false)
      }
    }

    target.tasks.named<Task>("check") {
      dependsOn(codeCoverageReport)
    }
  }
}

private fun JacocoReport.reportsKt(configuration: JacocoReportsContainer.() -> Unit): JacocoReportsContainer =
  this.reports(configuration)
