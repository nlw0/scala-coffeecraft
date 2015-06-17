package coffeecraft.dao

import akka.actor.Actor
import akka.pattern.pipe
import coffeecraft.models._
import slick.driver.H2Driver.api._


object InventoryItemDao {

  val table = TableQuery[InventoryItems]

  val db = Database.forConfig("h2mem1")

  def insert(newItem: InventoryItem) = run(Insert(newItem)).mapTo[Unit]

  def fetch(id: Int) = run(Fetch(id)).mapTo[Vector[InventoryItem]]

  def remove(id: Int) = run(Remove(id)).mapTo[Int]

  def update(id: Int, newInventoryItemData: InventoryItem) = run(Update(id, newInventoryItemData)).mapTo[Int]

  def fetchAll() = run(FetchAll).mapTo[Vector[InventoryItem]]

  def run(cmd: Command) = db.run(query(cmd))

  private def query(cmd: Command) = cmd match {
    case Insert(newInventoryItem: InventoryItem) =>
      DBIO.seq(table += newInventoryItem)
    case Fetch(id) =>
      table.filter(_.id === id).result
    case Remove(id) =>
      table.filter(_.id === id).delete
    case FetchAll =>
      table.result
  }
}


class InventoryItemDaoHandler extends Actor {

  import context.dispatcher

  def receive = {
    case CommandMsg(cmd, who) =>
      pipe(InventoryItemDao.run(cmd)) to who
  }
}