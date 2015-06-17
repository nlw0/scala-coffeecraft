package coffeecraft.models

import slick.driver.H2Driver.api._


case class Coffee(name: String, price: Double, id: Option[CoffeeKey] = None) {
  def updateID(newID: CoffeeKey) = Coffee(name, price, id = Some(newID))
}

class Coffees(tag: Tag) extends Table[Coffee](tag, "COFFEES") {
  def id = column[CoffeeKey]("ID", O.PrimaryKey, O.AutoInc)

  def name = column[String]("NAME")

  def price = column[Double]("PRICE")

  def * = (name, price, id.?) <>(Coffee.tupled, Coffee.unapply)
}


case class InventoryItem(userID: CoffeeKey, coffeID: CoffeeKey, id: Option[CoffeeKey] = None)

class InventoryItems(tag: Tag) extends Table[InventoryItem](tag, "INVENTORY") {
  val coffees = TableQuery[Coffees]

  def id = column[CoffeeKey]("ID", O.PrimaryKey, O.AutoInc)

  def userID = column[CoffeeKey]("USER_ID")

  def coffeeID = column[CoffeeKey]("COFFEE_ID")

  def coffee = foreignKey("COFFEE_FK", coffeeID, coffees)(_.id)

  def * = (userID, coffeeID, id.?) <>(InventoryItem.tupled, InventoryItem.unapply)
}
