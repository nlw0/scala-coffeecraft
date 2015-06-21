package coffeecraft.models

import slick.driver.H2Driver.api._


case class Recipe(coffeeId: Long, id: Option[Long])


class Recipes(tag: Tag) extends Table[Recipe](tag, "RECIPES") {

  val coffees = TableQuery[Coffees]

  def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

  def coffeeId = column[Long]("COFFEE_ID")

  def coffee = foreignKey("RECIPE_COFFEE_FK", coffeeId, coffees)(_.id)

  def * = (coffeeId, id.?) <>(Recipe.tupled, Recipe.unapply)
}

