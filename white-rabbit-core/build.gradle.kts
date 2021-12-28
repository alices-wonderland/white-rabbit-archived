buildscript {
  dependencies {
    classpath("org.hibernate:hibernate-gradle-plugin:5.6.3.Final")
  }
}

plugins {
  id("library-configuration")
}

apply(plugin = "org.hibernate.orm")

dependencies {
  api("org.springframework.boot:spring-boot-starter-validation")
  api("org.springframework.boot:spring-boot-starter-data-jpa")
  api("org.springframework.boot:spring-boot-starter-security")
  api("com.querydsl:querydsl-jpa")
  runtimeOnly("org.postgresql:postgresql")

  api("io.projectreactor:reactor-core")

  annotationProcessor("com.querydsl:querydsl-apt:5.0.0:jpa")
  annotationProcessor("jakarta.persistence:jakarta.persistence-api:2.2.3")
}

extensions.configure<org.hibernate.orm.tooling.gradle.HibernateExtension> {
  enhance(closureOf<org.hibernate.orm.tooling.gradle.EnhanceExtension> {
    enableLazyInitialization = true
    enableDirtyTracking = true
    enableAssociationManagement = true
    enableExtendedEnhancement = false
  })
}
