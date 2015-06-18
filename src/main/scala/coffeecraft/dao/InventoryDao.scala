package coffeecraft.dao

import coffeecraft.models._
import slick.driver.H2Driver.api._

import scala.concurrent.Future


object InventoryDao {

  val table = TableQuery[Inventories]

  val db = Database.forConfig("h2mem1")

  def filterQuery(id: Long): Query[Inventories, Inventory, Seq] =
    table.filter(_.id === id)

  def fetchById(id: Long): Future[Inventory] =
    try db.run(filterQuery(id).result.head)
    finally db.close()

  def insert(item: Inventory): Future[Int] =
    try db.run(table += item)
    finally db.close()

  def update(id: Long, item: Inventory): Future[Int] =
    try db.run(filterQuery(id).update(item))
    finally db.close()

  def remove(id: Long): Future[Int] =
    try db.run(filterQuery(id).delete)
    finally db.close()
}
