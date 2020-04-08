package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.Console.println

class TsvFeederConvert extends Simulation {

  val httpConf = http.baseUrl("http://localhost:8080/app/")
    .header("Accept", "application/json")
    .proxy(Proxy("localhost", 8888))


object FixW {

  private val tsvFeeder = tsv("data/fix_w.tsv").circular


  val read =
    feed(tsvFeeder)

  def saveTagAs(tag: String, name: String) = {
    exec {
      session =>
        val fixParsed = messageMap(session.attributes("fix_w").toString)
        session.set(name, fixParsed.getOrElse(tag, throw new RuntimeException(s"Can't find tag ${tag} in FIX message: ${session.attributes("fix_w").toString}")))
    }
  }

  def generateCheckSum = {
    exec {
      session =>
        val fixBody = session.attributes("fix_w").toString.split("\\u000110=")(0)

        var checkSum = 0
        for(i <- 0 until fixBody.length){
          checkSum += fixBody(i)
        }
        checkSum += '\u0001'
        checkSum %= 256
        val newFix = fixBody + '\u0001' + "10=%03d".format(checkSum) + '\u0001'
        println(checkSum)

        //session.set(name, fixParsed.getOrElse(tag, throw new RuntimeException(s"Can't find tag ${tag} in FIX message: ${session.attributes("fix_w").toString}")))
        session
    }
  }

  private def messageMap(contents: String): Map[String, String] =
    contents.split('\u0001').map(_.split('=')).collect { case Array(k, v) => k -> v }.toMap
}



  def getSpecificVideoGame() = {
    repeat(1) {
      exec(
        FixW.read,
        FixW.generateCheckSum,
        FixW.saveTagAs("34", "MsgSeqNum")
        .exec {
            session =>
              println(session)
              println(session("MsgSeqNum"))
              session
          }
        .exec(http("Get specific video game")
            .get("videogames/${MsgSeqNum}")
            .check(status.is(200))))
    }
  }

  val scn = scenario("Tsv Feeder test")
    .exec(getSpecificVideoGame())



  setUp(
    scn.inject(atOnceUsers(1))
  ).protocols(httpConf)

}
