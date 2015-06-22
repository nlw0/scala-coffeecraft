package coffeecraft.models

import akka.actor.{Actor, PoisonPill}
import coffeecraft.dao.InventoryDao

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


class UserInventory(userId: Long) extends Actor {

  import coffeecraft.models.UserInventory._

  InventoryDao.fetchInventory(userId) onComplete {
    case Success(inventory) =>
      context.become(withInventory(inventory.toMap))
    case Failure(e) =>
      println("Cannot initialize actor")
      self ! PoisonPill
  }

  def receive = {
    case _ =>
  }

  def withInventory(inventory: Map[Long, Coffee]): Receive = {
    case ListCmd =>
      sender ! inventory

    case MineCmd =>
      val mineResult = CraftingProcessor.mine()
      if (mineResult.isDefined) {
        context.become(withInventory(inventory + (inventory.keys.max + 1L -> mineResult.get)))
      }
      sender ! mineResult

    case CraftCmd(ii) =>
      val ingredients = CoffeeIdSet(ii map (inventory(_).id.get))
      val craftResult = CraftingProcessor.craft(ingredients)
      if (craftResult.isDefined) {
        context.become(withInventory((inventory -- ii) + (inventory.keys.max + 1L -> craftResult.get)))
      }
      sender ! craftResult

  }
}

object UserInventory {

  case class CraftCmd(items: Set[Long])

  case object ListCmd

  case object MineCmd

  case object ActionAck

}
