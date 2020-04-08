package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.Console.println

class CheckResponseBodyAndExtractXpath extends Simulation {

  val httpConf = http.baseUrl("http://localhost:8080/app/")
    .header("Accept", "application/xml")

  val scn = scenario("Check XPath and Regex")

      .exec(http("Get specific game")
      .get("videogames/1")
      .check(xpath("//name/text()").is("Resident Evil 4"))
      .check(regex("""(?<=<name>)(.*)(?=<\/name>)""").find.is("Resident Evil 4"))

      .check(bodyString.saveAs("responseBody")))
      .exec { session => println(s"Response body: ${session("responseBody").as[String]}"); session}


  setUp(
    scn.inject(atOnceUsers(1))
  ).protocols(httpConf)

}
