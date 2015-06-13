package coffeecraft.models

import slick.driver.H2Driver.api._

case class Coffee(name: String, price: Double, id: Option[Int] = None)

class Coffees(tag: Tag) extends Table[Coffee](tag, "FOODS") {
  // Auto Increment the id primary key column
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

  def name = column[String]("NAME")

  def price = column[Double]("PRICE")

  // the * projection (e.g. select * ...) auto-transforms the tupled
  // column values to / from a User
  def * = (name, price, id.?) <> (Coffee.tupled, Coffee.unapply)
}
