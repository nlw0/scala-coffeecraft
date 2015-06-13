name := "coffeecraft"

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies ++= List(
  "org.scala-lang" % "scala-reflect" % "2.11.6",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.3",
  "com.typesafe.slick" %% "slick" % "3.0.0-RC1",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.h2database" % "h2" % "1.3.175",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "com.typesafe.akka" %% "akka-actor" % "2.4-M1",
  "com.typesafe.akka" %% "akka-camel" % "2.4-M1",
  "org.apache.camel" % "camel-stream" % "2.15.2",
  "org.apache.camel" % "camel-netty" % "2.15.2"
  //"org.apache.camel" % "camel-mina2" % "2.15.2",
  //"org.apache.camel" % "camel-jetty" % "2.15.2"
)

fork in run := true
