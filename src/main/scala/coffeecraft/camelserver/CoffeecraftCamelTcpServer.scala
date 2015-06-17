package coffeecraft.camelserver

import akka.actor.Props
import akka.camel.{CamelMessage, Consumer}
import coffeecraft.dao.{CoffeeDaoHandler, CommandMsg, InventoryItemDaoHandler}
import coffeecraft.models._

class CoffeecraftCamelTcpServer extends Consumer {
  def endpointUri = "netty:tcp://localhost:60001?textline=true"

  val dbh = context.actorOf(Props[CoffeeDaoHandler])
  val invh = context.actorOf(Props[InventoryItemDaoHandler])

  def receive = {
    case msg: CamelMessage =>
      println(s"received ${msg.bodyAs[String]}")

      MessageTranslator(msg.bodyAs[String]) match {
        case (ToCoffee, mm) => dbh ! CommandMsg(mm, sender)
        case (ToInventory, mm) => invh ! CommandMsg(mm, sender)
      }
  }
}
