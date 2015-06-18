package coffeecraft.dao

import coffeecraft.models._
import slick.driver.H2Driver.api._

import scala.concurrent.Future


class GenericDao[C <: HasId, Cs <: Table[C] with HasIdColumn[C]](table: TableQuery[Cs]) {

  //val table = TableQuery[Cs]

  val db = Database.forConfig("h2mem1")

  def filterQuery(id: CoffeeKey) = //: Query[Cs, C, Seq] =
    table.filter(_.id === id)

  def fetchAll() =
    try db.run(table.filter(x => x.id === x.id).result)
    finally db.close()

  def fetchById(id: CoffeeKey): Future[C] =
    try db.run(filterQuery(id).result)
    finally db.close()

  def insert(item: C): Future[Int] =
    try db.run(table += item)
    finally db.close()

  def update(id: Long, item: C): Future[Int] =
    try db.run(filterQuery(id).update(item))
    finally db.close()

  def remove(id: Long): Future[Int] =
    try db.run(filterQuery(id).delete)
    finally db.close()
}


object CoffeeDao extends GenericDao[Coffee, Coffees](TableQuery[Coffees])