package coffeecraft.models

import akka.actor.{Actor, PoisonPill}
import coffeecraft.dao.InventoryDao

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


class UserInventory(userId: Long) extends Actor {

  import coffeecraft.models.UserInventory._

  InventoryDao.fetchInventory(userId) onComplete {
    case Success(inventory) =>
      context.become(withInventory(10.0, inventory.toMap))
    case Failure(e) =>
      println("Cannot initialize actor")
      self ! PoisonPill
  }

  def receive = {
    case _ =>
  }

  def withInventory(money: Double, inventory: Map[Long, Coffee]): Receive = {
    case ListCmd =>
      sender !(money, inventory)

    case MineCmd =>
      val mineResult = CraftingProcessor.mine(forFree = money < 2.0)
      context.become(
        withInventory(
          if (money > 2.0) money - 2.0 else money,
          if (mineResult.isDefined) inventory + (inventory.keys.max + 1L -> mineResult.get)
          else inventory
        )
      )
      sender ! mineResult

    case CraftCmd(is) =>
      val ingredients = CoffeeIdSet(is map (inventory(_).id.get))
      val craftResult = CraftingProcessor.craft(ingredients)
      if (craftResult.isDefined) {
        context.become(
          withInventory(money, (inventory -- is) + (inventory.keys.max + 1L -> craftResult.get))
        )
      }
      sender ! craftResult

    case SellCmd(ii) =>
      inventory.get(ii) match {
        case Some(cc) =>
          context.become(withInventory(money + cc.price, inventory - ii))
          sender ! Some(cc.price)
        case None =>
          sender ! None
      }
  }
}

object UserInventory {

  case object ListCmd

  case object MineCmd

  case class CraftCmd(items: Set[Long])

  case class SellCmd(item: Long)

  case object ActionAck

}
