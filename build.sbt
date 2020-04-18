name := "shop-scrapper"

version := "0.1"

scalaVersion := "2.13.1"
scalacOptions += "-language:postfixOps"

libraryDependencies ++= Seq(
  "net.ruippeixotog"   %% "scala-scraper"    % "2.2.0",
  "com.typesafe.akka"  %% "akka-actor-typed" % "2.6.4",
  "com.typesafe.slick" %% "slick"            % "3.3.2",
  "com.typesafe.slick" %% "slick-hikaricp"   % "3.3.2",
  "org.slf4j"          % "slf4j-nop"         % "1.6.4",
  "org.xerial"         % "sqlite-jdbc"       % "3.8.11.2",
  "org.scalaj"         % "scalaj-http_2.13"  % "2.4.2"
)
