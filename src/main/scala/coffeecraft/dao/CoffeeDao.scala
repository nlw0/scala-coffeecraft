package coffeecraft.dao

import coffeecraft.models._
import slick.driver.H2Driver.api._


object CoffeeDao extends GenericDao[Coffee, Coffees, Long] {
  override val table = TableQuery[Coffees]

  override def filterQuery(id: Long): Query[Coffees, Coffee, Seq] = table.filter(_.id === id)
}
