package coffeecraft.dao

import coffeecraft.models._
import slick.driver.H2Driver.api._


object InventoryDao extends GenericDao[Inventory, Inventories, Long] {
  override val table = TableQuery[Inventories]

  override def filterQuery(id: Long): Query[Inventories, Inventory, Seq] = table.filter(_.id === id)
}
