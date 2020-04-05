name := "shop-scrapper"

version := "0.1"

scalaVersion := "2.13.1"
scalacOptions += "-language:postfixOps"

libraryDependencies ++= Seq(
  "net.ruippeixotog"  %% "scala-scraper"    % "2.2.0",
  "com.typesafe.akka" %% "akka-actor-typed" % "2.6.4"
)
