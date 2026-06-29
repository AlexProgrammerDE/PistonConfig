description = "YAML backend for PistonConfig."

dependencies {
  api(project(":pistonconfig-core"))
  implementation("org.yaml:snakeyaml:2.5")
}
