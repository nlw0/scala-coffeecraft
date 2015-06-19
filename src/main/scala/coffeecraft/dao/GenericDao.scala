package coffeecraft.dao

import coffeecraft.models._
import slick.driver.H2Driver.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


abstract class GenericDao[E <: EntityWithId, T <: TableWithId[E], K] {

  val table: TableQuery[T]

  def filterQuery(id: K): Query[T, E, Seq]

  val db = Database.forConfig("h2mem1")

  def fetchAll() =
    db.run(table.result)

  def fetchById(id: K): Future[Option[E]] =
    db.run(filterQuery(id).result).map(_.headOption)

  def insert(item: E): Future[Int] =
    db.run(table += item)

  def update(id: K, item: E): Future[Int] =
    db.run(filterQuery(id).update(item))

  def remove(id: K): Future[Int] =
    db.run(filterQuery(id).delete)

}
