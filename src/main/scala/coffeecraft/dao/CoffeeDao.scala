package coffeecraft.dao

import akka.actor.{Actor, ActorRef}
import akka.pattern.pipe
import coffeecraft.models._
import slick.driver.H2Driver.api._


object CoffeeDao {

  val table = TableQuery[Coffees]

  val db = Database.forConfig("h2mem1")

  def insert(newCoffee: Coffee) = run(Insert(newCoffee)).mapTo[Unit]

  def fetch(id: Int) = run(Fetch(id)).mapTo[Vector[Coffee]]

  def remove(id: Int) = run(Remove(id)).mapTo[Int]

  def update(id: Int, newCoffeeData: Coffee) = run(Update(id, newCoffeeData)).mapTo[Int]

  def fetchAll() = run(FetchAll).mapTo[Vector[Coffee]]

  def run(cmd: Command) = db.run(query(cmd))

  private def query(cmd: Command) = cmd match {
    case Insert(newCoffee: Coffee) =>
      DBIO.seq(table += newCoffee)
    case Fetch(id) =>
      table.filter(_.id === id).result
    case Remove(id) =>
      table.filter(_.id === id).delete
    case Update(id, newCoffeeData: Coffee) =>
      table.filter(_.id === id).update(newCoffeeData.updateId(id))
    case FetchAll =>
      table.result
  }
}


class CoffeeDaoHandler extends Actor {

  import context.dispatcher

  def receive = {
    case CommandMsg(cmd, who) =>
      pipe(CoffeeDao.run(cmd)) to who
  }
}

trait Command

case class Fetch(id: Int) extends Command

case class Remove(id: Int) extends Command

case class Insert[C](newElement: C) extends Command

case class Update[C](id: Int, newElement: C) extends Command

case object FetchAll extends Command

case object CommandError extends Command

case class CommandMsg(cmd: Command, sender: ActorRef)
