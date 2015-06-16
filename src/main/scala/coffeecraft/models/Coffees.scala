package coffeecraft.models

import slick.driver.H2Driver.api._


case class Coffee(name: String, price: Double, id: Option[CoffeeKey] = None) {
  def updateId(newId: CoffeeKey) = Coffee(name, price, id = Some(newId))
}

class Coffees(tag: Tag) extends Table[Coffee](tag, "COFFEES") {
  // Auto Increment the id primary key column
  def id = column[CoffeeKey]("ID", O.PrimaryKey, O.AutoInc)

  def name = column[String]("NAME")

  def price = column[Double]("PRICE")

  // the * projection (e.g. select * ...) auto-transforms the tupled
  // column values to / from a case class
  def * = (name, price, id.?) <> (Coffee.tupled, Coffee.unapply)
}
