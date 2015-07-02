package coffeecraft.models

import coffeecraft.dao.{CoffeeDao, RecipeDao}

import scala.concurrent.Await
import scala.concurrent.duration._

case class CoffeeIdSet(value: Set[Long]) extends AnyVal

class BaseCraftingProcessor(val craftingFunction: Map[CoffeeIdSet, Coffee]) {
  def craft(input: CoffeeIdSet) = {
    craftingFunction.get(input)
  }

  // TODO: retrieve mining probabilities from some table
  def mine(forFree: Boolean = false) = {
    val idLimit = if (forFree) 3 else 6
    scala.util.Random.nextInt(3) match {
      case 0 => None
      case 1 =>
        Await.result(CoffeeDao.fetchOneById(1L), Duration.Inf)
      case 2 =>
        val newId = scala.util.Random.nextInt(idLimit).toLong + 1L
        Await.result(CoffeeDao.fetchOneById(newId), Duration.Inf)
    }
  }
}

object CraftingProcessor extends BaseCraftingProcessor(Await.result(RecipeDao.assembleCraftFunction(), Duration.Inf))