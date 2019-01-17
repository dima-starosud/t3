name := "t3"

version := "1.0"

scalaVersion := "2.12.8"

scalacOptions ++= Seq(
  "-unchecked",
  "-feature",
  "-deprecation",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Xlog-implicits",
)

scalacOptions += "-Ypartial-unification"

libraryDependencies ++= Seq(
  "com.googlecode.lanterna" % "lanterna" % "3.0.1",
  "io.reactivex" %% "rxscala" % "0.26.5",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
  "org.typelevel" %% "cats-core" % "1.5.0",
  "junit" % "junit" % "4.11" % "test",
  "org.scalatest" % "scalatest_2.12" % "3.0.5" % "test",
  "org.scalacheck" %% "scalacheck" % "1.14.0" % "test",
)

addCompilerPlugin(
  "org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full
)

libraryDependencies += "com.github.mpilquist" %% "simulacrum" % "0.14.0"
