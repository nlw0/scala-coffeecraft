package coffeecraft.models

import slick.driver.H2Driver.api._


case class Inventory(userId: Long, coffeId: Long, id: Option[Long] = None) extends EntityWithId


class Inventories(tag: Tag) extends TableWithId[Inventory](tag, "INVENTORIES") {

  val coffees = TableQuery[Coffees]

  def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

  def userId = column[Long]("USER_ID")

  def coffeeId = column[Long]("COFFEE_ID")

  def coffee = foreignKey("INVENTORY_COFFEE_FK", coffeeId, coffees)(_.id)

  def * = (userId, coffeeId, id.?) <>(Inventory.tupled, Inventory.unapply)
}
