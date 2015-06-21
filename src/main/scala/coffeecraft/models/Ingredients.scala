package coffeecraft.models

import slick.driver.H2Driver.api._


case class Ingredient(coffeeId: Long, id: Option[Long])


class Ingredients(tag: Tag) extends Table[Ingredient](tag, "RECIPE_INGREDIENTS") {

  val coffees = TableQuery[Coffees]

  val recipes = TableQuery[Recipes]

  def id = column[Long]("RECIPE_ID")

  def recipe = foreignKey("RECIPE_FK", id, recipes)(_.id)

  def coffeeId = column[Long]("COFFEE_ID")

  def ingredient = foreignKey("INGREDIENT_COFFEE_FK", coffeeId, coffees)(_.id)

  def * = (coffeeId, id.?) <>(Ingredient.tupled, Ingredient.unapply)
}
