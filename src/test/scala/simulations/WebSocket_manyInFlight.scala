package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.Console.println
import scala.concurrent.duration._


class WebSocket_manyInFlight extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:9000")
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Gatling2")
    .wsBaseUrl("ws://localhost:9000")
 //   .proxy(Proxy("localhost", 8888))

  val scn = scenario("WebSocket")
//    .exec(http("Home").get("/"))
    .exec(session => session.set("id", "User" + session.userId))
//    .pause(0,2)
    .exec(ws("Connect WS").connect("/chat"))
    .repeat(2000, "i") {
      exec(ws("Send message and wait for response")
        .sendText("Hello, I'm ${id} and this is message ${i}!")
    )
    //.exec { session => println(s"ReceivedMessage: ${session("receivedMessage").as[String]}"); session}
    }
    //.exec(ws("Close WS").close)

  // 3 Load Scenario
  setUp(
    scn.inject(atOnceUsers(1))
  ).protocols(httpProtocol)

}
