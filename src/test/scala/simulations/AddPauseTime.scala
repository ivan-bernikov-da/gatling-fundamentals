package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration.DurationInt

class AddPauseTime extends Simulation {

  val httpConf = http.baseUrl("http://localhost:8000")
   //.header("Accept", "application/json")

  val scn = scenario("Video Game DB - 3 calls")

    .exec(http("Get all video games - 1st call")
    .get("videogames"))
    .pause(5)

    .exec(http("Get specific game")
    .get("videogames/1"))
    .pause(1, 20)

    .exec(http("Get all Video games - 2nd call")
    .get("videogames"))
    .pause(3000.milliseconds)


  val scn2 = scenario("Simple Http server")
      .repeat(100) {
        exec(http("Get")
          .get("/")
          .check(status is 200))
          .exec(http("post")
            .post("/").formParam("test", "testvalue"))
      }

  setUp(
    scn2.inject(atOnceUsers(200))
  ).protocols(httpConf)

}
