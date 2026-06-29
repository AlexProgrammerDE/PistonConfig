description = "TOML backend for PistonConfig."

dependencies {
  api(project(":pistonconfig-core"))
  implementation("com.electronwill.night-config:core:3.9.0")
  implementation("com.electronwill.night-config:toml:3.9.0")
}
