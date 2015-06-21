package coffeecraft.dao

import coffeecraft.models._
import slick.driver.H2Driver.api._


object IngredientDao extends GenericDao[Ingredient, Ingredients, Long] {
  override val table = TableQuery[Ingredients]

  override def filterQuery(id: Long): Query[Ingredients, Ingredient, Seq] = table.filter(_.id === id)
}
