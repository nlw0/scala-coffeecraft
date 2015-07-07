package coffeecraft.models

import akka.actor.{Actor, ActorRef}
import akka.pattern.pipe
import coffeecraft.dao.{CoffeeDao, RecipeDao}
import coffeecraft.models.CraftingProcessor._

import scala.concurrent.duration._
import scala.language.postfixOps

case class CoffeeIdSet(value: Set[Long]) extends AnyVal

class CraftingProcessor extends Actor {

  import context.dispatcher

  self ! UpdateCraftingFunctionTicker

  def receive = processor(Map())

  def processor(craftingFunction: Map[CoffeeIdSet, Coffee]): Receive = {
    case ProcessorCraftCmd(who, ingredients, indices) =>
      sender ! ProcessorCraftReply(who, craftingFunction.get(ingredients), indices)

    case ProcessorMineCmd(who, forFree) =>
      val idLimit = if (forFree) 3 else 6
      scala.util.Random.nextInt(4) match {
        case 0 =>
          sender ! ProcessorMineReply(who, None)
        case 1 =>
          val newId = scala.util.Random.nextInt(3).toLong + 1L
          pipe(CoffeeDao.fetchOneById(newId) map (ProcessorMineReply(who, _))) to sender
        case _ =>
          val newId = scala.util.Random.nextInt(idLimit).toLong + 1L
          pipe(CoffeeDao.fetchOneById(newId) map (ProcessorMineReply(who, _))) to sender
      }

    case UpdateCraftingFunctionTicker =>
      pipe(RecipeDao.getCraftFunction() map UpdateCraftingFunction) to self
      context.system.scheduler.scheduleOnce(1 second, self, UpdateCraftingFunctionTicker)

    case UpdateCraftingFunction(newCraftingFunction: Map[CoffeeIdSet, Coffee]) =>
      context.become(processor(newCraftingFunction))
  }
}

object CraftingProcessor {

  case class ProcessorMineCmd(who: ActorRef, forFree: Boolean = false)

  case class ProcessorMineReply(who: ActorRef, coffee: Option[Coffee])

  case class ProcessorCraftCmd(who: ActorRef, input: CoffeeIdSet, indices: Set[Long])

  case class ProcessorCraftReply(who: ActorRef, coffee: Option[Coffee], indices: Set[Long])

  case class UpdateCraftingFunction(newFunction: Map[CoffeeIdSet, Coffee])

  case object UpdateCraftingFunctionTicker

}
