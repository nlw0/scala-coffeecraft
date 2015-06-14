package coffeecraft.dao

import akka.actor.{Actor, ActorRef}
import akka.pattern.pipe
import coffeecraft.models._
import slick.driver.H2Driver.api._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


object CoffeeDao {

  val coffees = TableQuery[Coffees]

  val db = Database.forConfig("h2mem1")

  val createTable = DBIO.seq(
    coffees.schema.create,
    coffees += Coffee("Milk", 2.0),
    coffees += Coffee("Coffee", 3.25),
    coffees += Coffee("Sparkling water", 1.20),
    coffees += Coffee("Jameson", 12.50)
  )

  Await.result(db.run(createTable), Duration.Inf)

  def run(cmd: Command) = {
    val query = cmd match {
      case Insert(newCoffee) =>
        DBIO.seq(coffees += newCoffee)
      case Fetch(id) =>
        coffees.filter(_.id === id).result
      case Remove(id) =>
        coffees.filter(_.id === id).delete
      case Update(id, newCoffeeData) =>
        coffees filter (_.id === id) update newCoffeeData.updateId(id)
      case FetchAll =>
        coffees.result
    }
    db.run(query)
  }
}

class CoffeeDaoHandler extends Actor {
  def receive = {
    case CommandMsg(cmd, who) =>
      pipe(CoffeeDao.run(cmd)) to who
  }
}

trait Command

case class Fetch(id: Int) extends Command

case class Remove(id: Int) extends Command

case class Insert(newCoffee: Coffee) extends Command

case class Update(id: Int, newCoffee: Coffee) extends Command

case object FetchAll extends Command

case object CommandError extends Command

case class CommandMsg(cmd: Command, sender: ActorRef)
