package coffeecraft.models

import coffeecraft.dao.{CoffeeDao, RecipeDao}

import scala.concurrent.Await
import scala.concurrent.duration._

case class CoffeeIdSet(value: Set[Long]) extends AnyVal

class BaseCraftingProcessor(val craftingFunction: Map[CoffeeIdSet, Coffee]) {
  def apply(input: CoffeeIdSet) = {
    craftingFunction.get(input)
  }

  def mine() = {
    scala.util.Random.nextInt(3) match {
      case x if x > 1 =>
        Await.result(CoffeeDao.fetchById(scala.util.Random.nextInt(2).toLong + 1L), Duration.Inf)
      case x if x > 0 =>
        Await.result(CoffeeDao.fetchById(scala.util.Random.nextInt(5).toLong + 1L), Duration.Inf)
      case _ => None
    }
  }
}

object CraftingProcessor extends BaseCraftingProcessor(Await.result(RecipeDao.assembleCraftFunction(), Duration.Inf))