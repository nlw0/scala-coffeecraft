package coffeecraft.camelserver

import akka.actor.Props
import akka.camel.{CamelMessage, Consumer}
import coffeecraft.InitDB
import coffeecraft.models._


class CoffeecraftCamelTcpServer extends Consumer {
  InitDB()

  def endpointUri = "netty:tcp://localhost:60001?textline=true"

  val invActor = context.actorOf(Props(classOf[UserInventory], 102))

  def receive = {
    case msg: CamelMessage =>
      // println(s"received ${msg.bodyAs[String]}")
      invActor forward MessageTranslator(msg.bodyAs[String])
  }
}


/*class UserInventory extends Actor {

  var inventory = Map[Long, Coffee](
    1L -> Coffee("Coffee", 2.50, Some(1L)),
    2L -> Coffee("Milk", 2.0, Some(2L)),
    3L -> Coffee("Milk", 2.0, Some(2L)),
    4L -> Coffee("Jameson", 12.50, Some(5L)),
    5L -> Coffee("Coffee", 2.50, Some(1L))
  )

  def receive = {
    case ListCmd =>
      sender ! "Inv: " + inventory

    case MineCmd =>
      val mineResult = CraftingProcessor.mine()
      if (mineResult.isDefined) inventory += (inventory.keys.max + 1L -> mineResult.get)
      sender ! "Res: " + mineResult

    case CraftCmd(ii) =>
      val ingredients = CoffeeIdSet(ii map (inventory(_).id.get))
      val craftResult = CraftingProcessor.craft(ingredients)
      if (craftResult.isDefined) inventory = (inventory -- ii) + (inventory.keys.max + 1L -> craftResult.get)
      sender ! "Res: " + craftResult
  }
}*/
