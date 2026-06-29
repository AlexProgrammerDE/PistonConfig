import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugins.signing.SigningExtension

plugins {
  id("io.freefair.lombok") version "9.5.0" apply false
}

val pistonConfigVersion = providers.gradleProperty("VERSION_NAME").get()
val immutablesVersion = "2.12.2"
val githubPackagesUrl = "https://maven.pkg.github.com/AlexProgrammerDE/PistonConfig"
val bomModuleName = "pistonconfig-bom"
val libraryModuleNames = listOf(
  "pistonconfig-core",
  "pistonconfig-properties",
  "pistonconfig-json",
  "pistonconfig-yaml",
  "pistonconfig-toml",
  "pistonconfig-hocon",
  "pistonconfig-annotations",
  "pistonconfig-env",
  "pistonconfig-static-fields",
  "pistonconfig-migrations",
)

allprojects {
  group = "net.pistonmaster"
  version = pistonConfigVersion
}

subprojects {
  val isBomModule = name == bomModuleName

  if (isBomModule) {
    apply(plugin = "java-platform")
  } else {
    apply(plugin = "java-library")
  }

  apply(plugin = "maven-publish")
  apply(plugin = "signing")

  val moduleName = name
  val moduleDisplayName = if (isBomModule) {
    "PistonConfig BOM"
  } else {
    val moduleTitle = moduleName.removePrefix("pistonconfig-")
      .split("-")
      .joinToString(" ") { word -> word.replaceFirstChar { character -> character.uppercase() } }
    "PistonConfig $moduleTitle"
  }
  val modulePomDescription = if (isBomModule) {
    "Bill of materials for aligning PistonConfig module versions."
  } else {
    "PistonConfig module: $moduleDisplayName"
  }

  if (isBomModule) {
    dependencies {
      constraints {
        libraryModuleNames.forEach { constrainedModuleName ->
          add("api", "net.pistonmaster:$constrainedModuleName:$pistonConfigVersion")
        }
      }
    }
  } else {
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
      add("testImplementation", platform("org.junit:junit-bom:6.1.1"))
      add("testImplementation", "org.junit.jupiter:junit-jupiter")
      add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
    }
  }

  extensions.configure<PublishingExtension>("publishing") {
    publications {
      register<MavenPublication>("mavenJava") {
        from(components[if (isBomModule) "javaPlatform" else "java"])
        artifactId = moduleName

        pom {
          name.set(moduleDisplayName)
          description.set(modulePomDescription)
          inceptionYear.set("2026")
          url.set("https://github.com/AlexProgrammerDE/PistonConfig")

          licenses {
            license {
              name.set("MIT License")
              url.set("https://opensource.org/license/mit")
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
      maven {
        name = "GitHubPackages"
        url = uri(githubPackagesUrl)
        credentials {
          username = providers.gradleProperty("githubPackagesUsername")
            .orElse(providers.environmentVariable("GITHUB_ACTOR"))
            .orNull
          password = providers.gradleProperty("githubPackagesToken")
            .orElse(providers.environmentVariable("GITHUB_TOKEN"))
            .orNull
        }
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
