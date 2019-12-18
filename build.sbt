name := "Scala_UE01"

version := "0.1"

scalaVersion := "2.13.1"

scalacOptions += "-deprecation"

val akkaVersion = "2.6.0"
val rxscalaVersion = "0.26.5"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % akkaVersion
