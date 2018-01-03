import com.typesafe.sbt.packager.docker._

name := "Integration test example"

lazy val akkaHttpVersion = "10.0.11"
lazy val akkaVersion    = "2.5.8"
lazy val commonSettings = Seq(
    organization    := "my.andrey",
    scalaVersion    := "2.12.4",
    version := "0.1"
)

lazy val installJavaInDocker = Seq(
      Cmd("USER", "root"),
      ExecCmd("RUN", "apt-get", "update"),
      ExecCmd("RUN", "apt-get", "install", "-y", "openjdk-8-jre")
    )

lazy val dockerSettingsApp = Seq(
    packageName in Docker := "scala-integrationtest-example-app",
    version in Docker := version.value,
    dockerBaseImage := "ubuntu:16.04",
    dockerCommands ++= installJavaInDocker,
    dockerExposedPorts += 8080
)

lazy val commonDependencies = Seq(
      "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
      "com.typesafe" % "config" % "1.3.2",
)

lazy val runApp = taskKey[Unit]("Custom run task for The-service that will let it finish on ENTER")

lazy val app = (project in file("app")).
  enablePlugins(DockerPlugin).
  enablePlugins(JavaAppPackaging).
  settings(
    inThisBuild(commonSettings),
    name := "the-service",
    dockerSettingsApp,
    libraryDependencies ++= commonDependencies,
    fullRunTask(runApp, Test, "WebServer", "ENTER")  
  )

lazy val tester = (project in file("tester")).
	settings(
	    inThisBuild(commonSettings),
	    name := "the-tester",
	    libraryDependencies ++= commonDependencies,
	    libraryDependencies ++= Seq(
          "org.scalatest" %% "scalatest" % "3.0.1" % Test,
          "org.testcontainers" % "testcontainers" % "1.5.1" % Test,
          "org.slf4j" % "slf4j-simple" % "1.7.25" % Test,
      ),
    
	)

// Additional tasks defined in this project
lazy val cleanAll = taskKey[Unit]("Cleans the compiled binaries and removes the Docker containers")
lazy val integrationTest = taskKey[Unit]("Builds the app, publishes Docker image locally and runs the integration tests")

cleanAll := {
  clean.in(app).value
  clean.in(tester).value
  clean.in(app, Docker).result.value
}

integrationTest := Def.sequential(
  publishLocal in (app, Docker),
  test in (tester, Test),
).value


