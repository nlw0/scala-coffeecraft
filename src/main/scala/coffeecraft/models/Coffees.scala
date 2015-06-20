package coffeecraft.models

import slick.driver.H2Driver.api._


case class Coffee(name: String, price: Double, id: Option[CoffeeKey] = None) extends EntityWithId {
  def updateId(newId: CoffeeKey) = Coffee(name, price, id = Some(newId))

  def craft(that: Coffee) = {

    val recipes = Map(
      List("Jameson", "Milk") -> "Irish",
      List("Chocolate", "Milk") -> "Cappuccino"
    )

    val result = recipes(List(this.name, that.name) sorted)
    Coffee(result, this.price + that.price)
  }
}


class Coffees(tag: Tag) extends TableWithId[Coffee](tag, "COFFEES") {

  def id = column[CoffeeKey]("Id", O.PrimaryKey, O.AutoInc)

  def name = column[String]("NAME")

  def price = column[Double]("PRICE")

  def * = (name, price, id.?) <>(Coffee.tupled, Coffee.unapply)
}
