package coffeecraft.dao

import coffeecraft.models._
import slick.driver.H2Driver.api._

import scala.concurrent.ExecutionContext.Implicits.global


object InventoryDao extends GenericDao[Inventory, Inventories, Long] {
  override val table = TableQuery[Inventories]

  override def filterQuery(id: Long): Query[Inventories, Inventory, Seq] = table.filter(_.userId === id)

  val coffees = TableQuery[Coffees]

  def fetchInventory(id: Long) = db.run(userInventoryQuery(id).result) map { xx =>
    xx map { case (ii, cc) => ii.index -> cc } toMap
  }

  private def userInventoryQuery(id: Long) = filterQuery(id) join coffees on (_.coffeeId === _.id)

}
