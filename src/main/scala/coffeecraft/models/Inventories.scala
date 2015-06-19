package coffeecraft.models

import slick.driver.H2Driver.api._


case class Inventory(userId: CoffeeKey, coffeId: CoffeeKey, id: Option[CoffeeKey] = None) extends EntityWithId


class Inventories(tag: Tag) extends TableWithId[Inventory](tag, "INVENTORY") {

  val coffees = TableQuery[Coffees]

  def id = column[CoffeeKey]("Id", O.PrimaryKey, O.AutoInc)

  def userId = column[CoffeeKey]("USER_ID")

  def coffeeId = column[CoffeeKey]("COFFEE_ID")

  def coffee = foreignKey("COFFEE_FK", coffeeId, coffees)(_.id)

  def * = (userId, coffeeId, id.?) <>(Inventory.tupled, Inventory.unapply)
}
