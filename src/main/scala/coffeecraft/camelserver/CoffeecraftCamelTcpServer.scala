package coffeecraft.camelserver

import akka.actor.Props
import akka.camel.{CamelMessage, Consumer}
import coffeecraft.InitDB
import coffeecraft.models._


class CoffeecraftCamelTcpServer extends Consumer {
  InitDB()

  def endpointUri = "netty:tcp://localhost:60001?textline=true"

  val invActor = context.actorOf(Props(classOf[UserInventory], 102L))

  def receive = {
    case msg: CamelMessage =>
      invActor forward MessageTranslator(msg.bodyAs[String])
  }
}
