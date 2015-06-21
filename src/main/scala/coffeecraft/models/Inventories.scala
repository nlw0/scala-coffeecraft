package coffeecraft.models

import slick.driver.H2Driver.api._


case class Inventory(userId: Long, coffeeId: Long, index: Long)


class Inventories(tag: Tag) extends Table[Inventory](tag, "INVENTORIES") {

  val coffees = TableQuery[Coffees]

  def userId = column[Long]("USER_ID")

  def index = column[Long]("INDEX")

  def coffeeId = column[Long]("COFFEE_ID")

  def coffee = foreignKey("INVENTORY_COFFEE_FK", coffeeId, coffees)(_.id)

  def pk = primaryKey("pk_a", (userId, index))

  def * = (userId, coffeeId, index) <>(Inventory.tupled, Inventory.unapply)
}
