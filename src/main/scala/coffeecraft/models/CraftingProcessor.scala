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
    if (scala.util.Random.nextInt(2) == 0)
      Await.result(CoffeeDao.fetchOneById(scala.util.Random.nextInt(idLimit).toLong + 1L), Duration.Inf)
    else None
  }
}

object CraftingProcessor extends BaseCraftingProcessor(Await.result(RecipeDao.assembleCraftFunction(), Duration.Inf))