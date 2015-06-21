package coffeecraft

import coffeecraft.models._
import slick.dbio.DBIO
import slick.driver.H2Driver.api._
import slick.lifted.TableQuery

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object InitDB {
  def apply() = {
    val coffees = TableQuery[Coffees]

    val inventories = TableQuery[Inventories]

    val recipes = TableQuery[Recipes]

    val ingredients = TableQuery[Ingredients]

    val db = Database.forConfig("h2mem1")

    val createTable = DBIO.seq(
      coffees.schema.create,
      inventories.schema.create,
      recipes.schema.create,
      ingredients.schema.create,

      coffees += Coffee("Coffee", 2.50, Some(1L)),
      coffees += Coffee("Milk", 2.0, Some(2L)),
      coffees += Coffee("Chocolate", 2.20, Some(3L)),
      coffees += Coffee("Lime", 1.20, Some(4L)),
      coffees += Coffee("Jameson", 12.50, Some(5L)),

      coffees += Coffee("Pingado", 4.50, Some(6L)),
      coffees += Coffee("Mocha", 6.00, Some(7L)),
      coffees += Coffee("Cappuccino", 12.00, Some(8L)),
      coffees += Coffee("Roman Coffee", 4.50, Some(9L)),
      coffees += Coffee("Irish Coffee", 19.00, Some(10L)),

      recipes += Recipe(6L, Some(1L)),
      recipes += Recipe(7L, Some(2L)),
      recipes += Recipe(8L, Some(3L)),
      recipes += Recipe(9L, Some(4L)),
      recipes += Recipe(10L, Some(5L)),

      ingredients += Ingredient(1L, Some(1L)),
      ingredients += Ingredient(2L, Some(1L)),
      ingredients += Ingredient(1L, Some(2L)),
      ingredients += Ingredient(3L, Some(2L)),
      ingredients += Ingredient(1L, Some(3L)),
      ingredients += Ingredient(2L, Some(3L)),
      ingredients += Ingredient(3L, Some(3L)),
      ingredients += Ingredient(1L, Some(4L)),
      ingredients += Ingredient(4L, Some(4L)),
      ingredients += Ingredient(1L, Some(5L)),
      ingredients += Ingredient(2L, Some(5L)),
      ingredients += Ingredient(5L, Some(5L)),

      inventories += Inventory(101, 2),
      inventories += Inventory(101, 3),
      inventories += Inventory(102, 1),
      inventories += Inventory(102, 4),
      inventories += Inventory(102, 4)
    )

    Await.result(db.run(createTable), Duration.Inf)
  }
}
