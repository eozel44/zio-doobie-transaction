import Dependencies._
import Dependencies.Libraries._

ThisBuild / scalaVersion := "2.13.6"
ThisBuild / version := "0.1.0-SNAPSHOT"


ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0"
ThisBuild / scalafixDependencies += "com.github.vovapolu"  %% "scaluzzi"         % "0.1.20"
ThisBuild / scalafixScalaBinaryVersion := CrossVersion.binaryScalaVersion(scalaVersion.value)

lazy val scalacopts = Seq(
  "-feature",
  "-deprecation",
  "-encoding","UTF-8",
  "-language:postfixOps",
  "-language:higherKinds",
  "-Wunused:imports",
  "-Ymacro-annotations"
)

lazy val mainProject = (project in file("."))
  .settings(
    name := "lmsbackend",
    libraryDependencies ++=  zio ++ doobie ++ zioLogging ++ circe,
    scalacOptions ++= scalacopts,
    fork:=true
  )

