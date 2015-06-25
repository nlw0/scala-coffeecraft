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

  implicit class InventoryMethods(inventory: Map[Long, Coffee]) {
    def plus(coffee: Option[Coffee]): Map[Long, Coffee] = coffee match {
      case Some(cc) =>
        val newIndex = Stream from 1 find (x => !(inventory contains x)) map (_.toLong) getOrElse 0L
        inventory + (newIndex -> cc)
      case None => inventory
    }
  }

  def withInventory(money: Double, inventory: Map[Long, Coffee]): Receive = {
    case ListCmd =>
      sender ! UserState(money, inventory)

    case MineCmd =>
      val newMoney = if (money < 2.0) money else roundMoney(money - 2.0)
      val mineResult = CraftingProcessor.mine(forFree = money < 2.0)
      context.become(withInventory(newMoney, inventory plus mineResult))
      sender ! (if (mineResult.isDefined) ActionACK else ActionNACK)

    case CraftCmd(is) =>
      val ingredients = CoffeeIdSet(is map (inventory(_).id.get))
      val craftResult = CraftingProcessor.craft(ingredients)
      if (craftResult.isDefined) context.become(withInventory(money, (inventory plus craftResult) -- is))
      sender ! (if (craftResult.isDefined) ActionACK else ActionNACK)

    case SellCmd(ii) =>
      val sellingItem = inventory.get(ii)
      for (cc <- sellingItem) context.become(withInventory(roundMoney(money + cc.price), inventory - ii))
      sender ! (if (sellingItem.isDefined) ActionACK else ActionNACK)
  }

  def roundMoney(x: Double) = math.round(x * 100.0) / 100.0
}

object UserInventory {

  case object ListCmd

  case object MineCmd

  case class CraftCmd(items: Set[Long])

  case class SellCmd(item: Long)

  case object ActionACK

  case object ActionNACK

  case class UserState(money: Double, inventory: Map[Long, Coffee])

}
