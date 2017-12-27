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
      //"com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      //"com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
      //"com.typesafe.akka" %% "akka-stream"          % akkaVersion,

      //"com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion % Test,
      //"com.typesafe.akka" %% "akka-testkit"         % akkaVersion     % Test,
      //"com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion     % Test,
      //"org.scalatest"     %% "scalatest"            % "3.0.1"         % Test
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
          "org.scalatest" %% "scalatest" % "3.0.1",
          "org.testcontainers" % "testcontainers" % "1.5.1"
      )
	)

