description = "HOCON backend for PistonConfig."

dependencies {
  api(project(":pistonconfig-core"))
  implementation("com.typesafe:config:1.4.9")
}
