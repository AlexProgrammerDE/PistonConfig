pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    mavenCentral()
  }
}

rootProject.name = "PistonConfig"

include(
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
