package coffeecraft.dao

import coffeecraft.models._
import slick.driver.H2Driver.api._


object CoffeeDao extends GenericDao[Coffee, Coffees, CoffeeKey] {
  override val table = TableQuery[Coffees]

  override def filterQuery(id: CoffeeKey): Query[Coffees, Coffee, Seq] =
    table.filter(_.id === id)
}
