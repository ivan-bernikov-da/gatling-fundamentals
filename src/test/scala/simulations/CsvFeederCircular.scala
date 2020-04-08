package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.Console.println

class CsvFeederCircular extends Simulation {

  val httpConf = http.baseUrl("http://localhost:8080/app/")
    .header("Accept", "application/json")


  val users = 1
  val repeatCount = 11
  var timestamp: Long = System.currentTimeMillis % 1000000000
  var idNumbers = (timestamp to timestamp+repeatCount*users).iterator

  val csvFeeder = csv("data/games.csv").circular

  val records = csv("data/games.csv")
    .batch(2)
    .readRecords

  def postNewGame() = {
    repeat(repeatCount) {
      feed(csvFeeder)
        .exec {
          session =>
            session.set("id", idNumbers.next().toString)
            //println(s"$timestamp")
            //session.set("id", (timestamp += 1).toString)
        }
        .exec {
          session =>
            println(session.toString)
            session
        }
        .exec(http("Post New Game")
          .post("videogames/")
            .body(StringBody(
                          "{" +
                          "\n\t\"id\": ${id}," +
                          "\n\t\"name\": \"${name}\"," +
                          "\n\t\"releaseDate\": \"${releaseDate}\"," +
                          "\n\t\"reviewScore\": ${reviewScore}," +
                          "\n\t\"category\": \"${category}\"," +
                          "\n\t\"rating\": \"${rating}\"\n}")
            ).asJson
          .check(status.is(200)))
    }
  }

  def postAllGamesFromFile() = {
    foreach(records, "record") {
      exec(flattenMapIntoAttributes("${record}"))
      .exec {
          session =>
            session.set("id", idNumbers.next().toString)
           // println(s"$timestamp")
           // session.set("gameId", (timestamp += 1).toString)
        }
      .exec {
        session =>
          println(session.toString)
        session
      }
        .exec(http("Post New Game")
          .post("videogames/")
          .body(StringBody(
            "{" +
              "\n\t\"id\": ${id}," +
              "\n\t\"name\": \"${name}\"," +
              "\n\t\"releaseDate\": \"${releaseDate}\"," +
              "\n\t\"reviewScore\": ${reviewScore}," +
              "\n\t\"category\": \"${category}\"," +
              "\n\t\"rating\": \"${rating}\"\n}")
          ).asJson
          .check(status.is(200)))
    }
  }

  val scn = scenario("Post new games")
    .exec(postNewGame())

  val scnAll = scenario("Post all games from feeder")
    .exec(postAllGamesFromFile())


  setUp(
    scnAll.inject(atOnceUsers(2))
  ).protocols(httpConf)

}
