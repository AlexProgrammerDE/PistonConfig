description = "Comment-aware Java properties backend for PistonConfig."

dependencies {
  api(project(":pistonconfig-core"))
  implementation("org.apache.commons:commons-configuration2:2.12.0")
}
