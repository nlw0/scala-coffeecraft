package coffeecraft.dao

import coffeecraft.models._
import slick.driver.H2Driver.api._

import scala.concurrent.ExecutionContext.Implicits.global


object InventoryDao extends GenericDao[Inventory, Inventories, (Long, Long)] {
  override val table = TableQuery[Inventories]

  override def filterQuery(pk: (Long, Long)): Query[Inventories, Inventory, Seq] =
    table filter { mm => mm.userId === pk._1 && mm.index === pk._2 }

  val coffees = TableQuery[Coffees]

  def fetchInventory(uid: Long) = db.run(userInventoryQuery(uid).result) map { xx =>
    xx map { case (ii, cc) => ii.index -> cc }
  }

  private def userInventoryQuery(uid: Long) =
    table.filter(mm => mm.userId === uid) join coffees on (_.coffeeId === _.id)

}
