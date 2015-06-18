package coffeecraft.models

import slick.driver.H2Driver.api._


trait HasId {
  type Id

  def id: Option[Id]
}

abstract class HasIdColumn[E <: HasId](tag: Tag, name: String) extends Table[E](tag, name) {
  def id = column[CoffeeKey]("ID", O.PrimaryKey, O.AutoInc)
}

case class Coffee(name: String, price: Double, id: Option[CoffeeKey] = None) extends HasId {
  def updateID(newID: CoffeeKey) = Coffee(name, price, id = Some(newID))
}

class Coffees(tag: Tag) extends HasIdColumn[Coffee](tag, "COFFEES") {
  def id = column[CoffeeKey]("ID", O.PrimaryKey, O.AutoInc)

  def name = column[String]("NAME")

  def price = column[Double]("PRICE")

  def * = (name, price, id.?) <>(Coffee.tupled, Coffee.unapply)
}


case class Inventory(userID: CoffeeKey, coffeID: CoffeeKey, id: Option[CoffeeKey] = None) extends HasId

class Inventories(tag: Tag) extends HasIdColumn[Inventory](tag, "INVENTORY") {
  val coffees = TableQuery[Coffees]

  def id = column[CoffeeKey]("ID", O.PrimaryKey, O.AutoInc)

  def userID = column[CoffeeKey]("USER_ID")

  def coffeeID = column[CoffeeKey]("COFFEE_ID")

  def coffee = foreignKey("COFFEE_FK", coffeeID, coffees)(_.id)

  def * = (userID, coffeeID, id.?) <>(Inventory.tupled, Inventory.unapply)
}


// class Coffees(tag: Tag) extends Table[Coffee](tag, "COFFEES") with HasIdColumn[Coffee] {
