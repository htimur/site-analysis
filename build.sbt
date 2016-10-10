name := """site-analysis"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  filters,
  cache,
  ws,
  "org.jsoup" % "jsoup" % "1.9.2",
  "com.adrianhurt" %% "play-bootstrap" % "1.1-P25-B3",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.11" % Test,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)



fork in run := true