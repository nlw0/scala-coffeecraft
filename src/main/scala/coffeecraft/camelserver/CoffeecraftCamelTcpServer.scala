package coffeecraft.camelserver

import akka.actor.Props
import akka.camel.{CamelMessage, Consumer}
import coffeecraft._
import coffeecraft.dao.{CommandMsg, CoffeeDaoHandler}
import coffeecraft.models._

class CoffeecraftCamelTcpServer extends Consumer {
  def endpointUri = "netty:tcp://localhost:60001?textline=true"

  val dbh = context.actorOf(Props[CoffeeDaoHandler])

  def receive = {
    case msg: CamelMessage =>
      println(s"received ${msg.bodyAs[String]}")
      dbh ! CommandMsg(MessageTranslator(msg.bodyAs[String]), sender)
  }
}
