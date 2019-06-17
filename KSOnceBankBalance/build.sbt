name := "KSOnceBankBalance"
organization := "com.sharop.scala.kafka.stream"
version := "0.1"

scalaVersion := "2.13.0"
mainClass := Some("com.sharop.scala.kafka.stream.BankBalance")


libraryDependencies ++= Seq(
  // https://mvnrepository.com/artifact/org.apache.kafka/kafka-clients
  "org.apache.kafka" % "kafka-clients" % "2.2.1",
  "org.apache.kafka" % "kafka-streams" % "2.2.1",
  "org.slf4j" % "slf4j-api" % "1.7.26",
  "org.slf4j" % "slf4j-log4j12" % "1.7.26"


)

libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.9"
libraryDependencies += "io.spray" %% "spray-json" % "1.3.5"
libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.9"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"

// add dependencies on standard Scala modules, in a way
// supporting cross-version publishing
// taken from: http://github.com/scala/scala-module-dependency-sample
libraryDependencies := {
  CrossVersion.partialVersion(scalaVersion.value) match {
    // if Scala 2.12+ is used, use scala-swing 2.x
    case Some((2, scalaMajor)) if scalaMajor >= 12 =>
      libraryDependencies.value ++ Seq(
        // https://mvnrepository.com/artifact/org.scala-lang.modules/scala-xml
        "org.scala-lang.modules" %% "scala-xml" % "1.2.0",
        // https://mvnrepository.com/artifact/org.scala-lang.modules/scala-parser-combinators
        "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2",
        "org.scala-lang.modules" %% "scala-swing" % "2.1.1")
    case Some((2, scalaMajor)) if scalaMajor >= 11 =>
      libraryDependencies.value ++ Seq(
        "org.scala-lang.modules" %% "scala-xml" % "1.0.6",
        "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4",
        "org.scala-lang.modules" %% "scala-swing" % "1.0.2")
    case _ =>
      // or just libraryDependencies.value if you don't depend on scala-swing
      libraryDependencies.value :+ "org.scala-lang" % "scala-swing" % scalaVersion.value



  }
}

// leverage java 8
javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")
scalacOptions := Seq("-target:jvm-1.8")
initialize := {
  val _ = initialize.value
  if (sys.props("java.specification.version") != "1.8")
    sys.error("Java 8 is required for this project.")
}