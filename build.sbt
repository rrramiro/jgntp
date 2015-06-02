
scalaVersion := "2.11.6"

organization := "com.google.code.sgntp"

name := "sgntp"

version := "1.2.2-SNAPSHOT"

sbtPlugin := true


//resolvers ++= Seq(
//  Resolver.sonatypeRepo("releases"),
//  Resolver.sonatypeRepo("snapshots")
//)

libraryDependencies ++= Seq(
  //"com.github.scopt" %% "scopt" % "3.3.0",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.slf4j" % "slf4j-api" % "1.6.1",
  "org.slf4j" % "slf4j-log4j12" % "1.6.1",
  "com.google.guava" % "guava" % "10.0.1",
  "org.jboss.netty" % "netty" % "3.2.6.Final",
  "commons-io" % "commons-io" % "2.4",
  //"io.reactivex" %% "rxscala" % "0.24.1",
  "junit" % "junit" % "4.12" % "test",
  "com.novocode" % "junit-interface" % "0.11" % "test"

)

//addCompilerPlugin("org.scalamacros" %% "paradise" % "2.0.1" cross CrossVersion.full)

