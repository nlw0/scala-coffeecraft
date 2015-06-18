/*
package coffeecraft.camelserver

import akka.actor.{Actor, Props}
import akka.camel.{CamelMessage, Consumer}
import coffeecraft.dao.{InventoryDao, CoffeeDao}
import coffeecraft.models._
import akka.pattern.pipe


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

class CoffeeDaoHandler extends Actor {

  import context.dispatcher

  def receive = {
    case CommandMsg(cmd, who) =>
      pipe(CoffeeDao.run(cmd)) to who
  }
}

class InventoryItemDaoHandler extends Actor {

  import context.dispatcher

  def receive = {
    case CommandMsg(cmd, who) =>
      pipe(InventoryDao.run(cmd)) to who
  }
}*/
