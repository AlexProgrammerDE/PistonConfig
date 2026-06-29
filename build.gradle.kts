import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugins.signing.SigningExtension

plugins {
  id("io.freefair.lombok") version "9.1.0" apply false
}

val pistonConfigVersion = providers.gradleProperty("VERSION_NAME").get()
val immutablesVersion = "2.12.2"

allprojects {
  group = "net.pistonmaster"
  version = pistonConfigVersion
}

subprojects {
  apply(plugin = "java-library")
  apply(plugin = "maven-publish")
  apply(plugin = "signing")

  val moduleName = name
  val moduleDisplayName = moduleName.replace("-", " ")

  extensions.configure<JavaPluginExtension>("java") {
    toolchain {
      languageVersion.set(JavaLanguageVersion.of(25))
    }

    withSourcesJar()
    withJavadocJar()
  }

  tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(25)
  }

  tasks.withType<Javadoc>().configureEach {
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).addBooleanOption("Xdoclint:all,-missing", true)
  }

  tasks.withType<Test>().configureEach {
    useJUnitPlatform()
  }

  dependencies {
    add("compileOnly", "org.immutables:value-annotations:$immutablesVersion")
    add("annotationProcessor", "org.immutables:value:$immutablesVersion")
    add("testImplementation", platform("org.junit:junit-bom:5.14.0"))
    add("testImplementation", "org.junit.jupiter:junit-jupiter")
    add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
  }

  extensions.configure<PublishingExtension>("publishing") {
    publications {
      register<MavenPublication>("mavenJava") {
        from(components["java"])
        artifactId = moduleName

        pom {
          name.set(moduleDisplayName)
          description.set("PistonConfig module: $moduleDisplayName")
          inceptionYear.set("2026")
          url.set("https://github.com/AlexProgrammerDE/PistonConfig")

          licenses {
            license {
              name.set("The Apache License, Version 2.0")
              url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
              distribution.set("repo")
            }
          }

          developers {
            developer {
              id.set("AlexProgrammerDE")
              name.set("AlexProgrammerDE")
              url.set("https://github.com/AlexProgrammerDE")
            }
          }

          scm {
            url.set("https://github.com/AlexProgrammerDE/PistonConfig")
            connection.set("scm:git:git://github.com/AlexProgrammerDE/PistonConfig.git")
            developerConnection.set("scm:git:ssh://git@github.com/AlexProgrammerDE/PistonConfig.git")
          }
        }
      }
    }

    repositories {
      maven {
        name = "staging"
        url = rootProject.layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()
      }
    }
  }

  extensions.configure<SigningExtension>("signing") {
    val signingKey = providers.gradleProperty("signingInMemoryKey")
      .orElse(providers.environmentVariable("ORG_GRADLE_PROJECT_signingInMemoryKey"))
    val signingPassword = providers.gradleProperty("signingInMemoryKeyPassword")
      .orElse(providers.environmentVariable("ORG_GRADLE_PROJECT_signingInMemoryKeyPassword"))

    setRequired {
      !version.toString().endsWith("-SNAPSHOT") && gradle.taskGraph.allTasks.any { task -> task.name.contains("publish", ignoreCase = true) }
    }

    if (signingKey.isPresent) {
      useInMemoryPgpKeys(signingKey.get(), signingPassword.orNull)
    }

    sign(extensions.getByType(PublishingExtension::class.java).publications)
  }
}
