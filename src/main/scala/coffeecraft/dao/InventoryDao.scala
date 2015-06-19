package coffeecraft.dao

import coffeecraft.models._
import slick.driver.H2Driver.api._


object InventoryDao extends GenericDao[Inventory, Inventories, CoffeeKey] {
  override val table = TableQuery[Inventories]

  override def filterQuery(id: CoffeeKey): Query[Inventories, Inventory, Seq] =
    table.filter(_.id === id)
}