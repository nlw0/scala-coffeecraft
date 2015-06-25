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
      sender ! UserState(money, inventory)

    case MineCmd =>
      val newMoney = if (money < 2.0) money else roundMoney(money - 2.0)
      val mineResult = CraftingProcessor.mine(forFree = money < 2.0)
      val newInventory = insert(inventory, mineResult)
      context.become(withInventory(newMoney, newInventory))
      sender ! (if (mineResult.isDefined) ActionACK else ActionNACK)

    case CraftCmd(is) =>
      val ingredients = CoffeeIdSet(is map (inventory(_).id.get))
      val craftResult = CraftingProcessor.craft(ingredients)
      if (craftResult.isDefined) {
        context.become(withInventory(money, insert(inventory, craftResult) -- is))
        sender ! ActionACK
      } else {
        sender ! ActionNACK
      }

    case SellCmd(ii) =>
      inventory.get(ii) match {
        case Some(cc) =>
          context.become(withInventory(roundMoney(money + cc.price), inventory - ii))
          sender ! ActionACK
        case None =>
          sender ! ActionNACK
      }
  }

  def insert(inventory: Map[Long, Coffee], coffee: Option[Coffee]) = coffee match {
    case Some(cc) =>
      val newIndex = Stream from 1 find (x => !(inventory contains x)) map (_.toLong) getOrElse 0L
      inventory + (newIndex -> cc)
    case None => inventory
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
