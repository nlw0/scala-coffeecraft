package coffeecraft.models

import slick.driver.H2Driver.api._


case class Coffee(name: String, price: Double, id: Option[CoffeeKey] = None) extends EntityWithId {
  def updateId(newId: CoffeeKey) = Coffee(name, price, id = Some(newId))
}


class Coffees(tag: Tag) extends TableWithId[Coffee](tag, "COFFEES") {

  def id = column[CoffeeKey]("Id", O.PrimaryKey, O.AutoInc)

  def name = column[String]("NAME")

  def price = column[Double]("PRICE")

  def * = (name, price, id.?) <>(Coffee.tupled, Coffee.unapply)
}
