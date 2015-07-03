package coffeecraft.models

import akka.actor.{Actor, PoisonPill}
import coffeecraft.dao.InventoryDao
import coffeecraft.models.CraftingProcessor.{ProcessorCraftCmd, ProcessorCraftReply, ProcessorMineCmd, ProcessorMineReply}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


class UserInventory(userId: Long) extends Actor {

  import coffeecraft.models.UserInventory._

  val craftingProcessor = context.actorSelection("../crafting-processor")

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
        val newIndex = Stream from 0 find (x => !(inventory contains x)) map (_.toLong) getOrElse 0L
        inventory + (newIndex -> cc)
      case None => inventory
    }
  }

  def withInventory(money: Double, inventory: Map[Long, Coffee]): Receive = {
    case ListCmd =>
      sender ! CoolUserState(money, inventory map { case (k, v) => InventoryItem(k, v) } toList)

    case MineCmd =>
      craftingProcessor ! ProcessorMineCmd(sender, forFree = money < 2.0)

    case ProcessorMineReply(who, mineResult) =>
      val newMoney = if (money < 2.0) money else roundMoney(money - 2.0)
      context.become(withInventory(newMoney, inventory plus mineResult))
      who ! (if (mineResult.isDefined) ActionACK else ActionNACK)

    case CraftCmd(ingredientIndices) =>
      val validIndices = ingredientIndices forall inventory.contains
      val ingredients = CoffeeIdSet(if (validIndices) ingredientIndices map (inventory(_).id.get) else Set())
      craftingProcessor ! ProcessorCraftCmd(sender, ingredients, ingredientIndices)

    case ProcessorCraftReply(who, craftResult, ingredientIndices) =>
      if (craftResult.isDefined)
        context.become(withInventory(money, (inventory plus craftResult) -- ingredientIndices))
      who ! (if (craftResult.isDefined) ActionACK else ActionNACK)

    case SellCmd(is) =>
      val validIndices = is forall inventory.contains
      val (newMoney, newInventory) = ((money, inventory) /: is) {
        case ((macc, invacc), ii) =>
          val sellingItem = inventory.get(ii)
          if (sellingItem.isDefined) (roundMoney(macc + sellingItem.get.price), invacc - ii)
          else (macc, invacc)
      }
      context.become(withInventory(newMoney, newInventory))
      sender ! (if (validIndices) ActionACK else ActionNACK)
  }

  def roundMoney(x: Double) = math.round(x * 100.0) / 100.0
}

object UserInventory {

  case object ListCmd

  case object MineCmd

  case class CraftCmd(items: Set[Long])

  case class SellCmd(item: Set[Long])

  case object ActionACK

  case object ActionNACK

  case class UserState(money: Double, inventory: Map[Long, Coffee])

  case class InventoryItem(index: Long, item: Coffee)

  case class CoolUserState(money: Double, inventory: Seq[InventoryItem])

}
