package coffeecraft.dao

import coffeecraft.domain.CoffeeIdSet
import coffeecraft.models._
import slick.driver.H2Driver.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps


object RecipeDao extends GenericDao[Recipe, Recipes, Long] {
  override val table = TableQuery[Recipes]

  val ingredients = TableQuery[Ingredients]

  val coffees = TableQuery[Coffees]

  override def filterQuery(id: Long): Query[Recipes, Recipe, Seq] = table.filter(_.id === id)

  def getCraftFunction = {
    val ingF = RecipeDao.recIngredients
    val outF = RecipeDao.recOutcomes
    for (ing <- ingF; out <- outF) yield out map { case (k, v) => CoffeeIdSet(ing(k).toSet) -> v } toMap
  }

  private def recIngredients = db.run(recIngredientsQuery.result) map { xx =>
    xx groupBy { case (ing, cof) => ing.id.get } map { case (k, v) => k -> v.map(_._2.id.get) }
  }

  private def recOutcomes = db.run(recOutcomesQuery.result) map { xx => xx map { case (k, v) => k.id.get -> v } }

  private def recIngredientsQuery = ingredients join coffees on (_.coffeeId === _.id)

  private def recOutcomesQuery = table join coffees on (_.coffeeId === _.id)
}
