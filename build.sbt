name := "Integration test example"
version := "0.1"

lazy val akkaHttpVersion = "10.0.11"
lazy val akkaVersion    = "2.5.8"
lazy val commonSettings = Seq(
    organization    := "my.andrey",
    scalaVersion    := "2.12.4"
)

lazy val commonDependencies = Seq(
      "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
      //"com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      //"com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
      //"com.typesafe.akka" %% "akka-stream"          % akkaVersion,

      //"com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion % Test,
      //"com.typesafe.akka" %% "akka-testkit"         % akkaVersion     % Test,
      //"com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion     % Test,
      //"org.scalatest"     %% "scalatest"            % "3.0.1"         % Test
)

lazy val app = (project in file("app")).
  settings(
    inThisBuild(commonSettings),
    name := "the-service",
    libraryDependencies ++= commonDependencies
  )

lazy val tester = (project in file("tester")).
	settings(
	    inThisBuild(commonSettings),
	    name := "the-tester",
	    libraryDependencies ++= commonDependencies,
	    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1"
	)