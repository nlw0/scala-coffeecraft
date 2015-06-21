package coffeecraft.models

import slick.driver.H2Driver.api._


case class Coffee(name: String, price: Double, id: Option[Long] = None) {
  def updateId(newId: Long) = Coffee(name, price, id = Some(newId))
}


class Coffees(tag: Tag) extends Table[Coffee](tag, "COFFEES") {

  def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

  def name = column[String]("NAME")

  def price = column[Double]("PRICE")

  def * = (name, price, id.?) <>(Coffee.tupled, Coffee.unapply)
}
