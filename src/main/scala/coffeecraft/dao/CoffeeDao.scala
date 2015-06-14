package coffeecraft.dao

import akka.actor.{Actor, ActorRef}
import akka.http.scaladsl.model.HttpResponse
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

  def get(id: Int) =
    run(Get(id)).mapTo[Vector[Coffee]] map (_.headOption)

  def delete(id: Int) = {
    run(Delete(id)).mapTo[Int] map {
      case affectedRowCount: Int if affectedRowCount > 0 =>
        HttpResponse(204)
      case _ =>
        HttpResponse(404)
    }
  }

  def post(newCoffee: Coffee) = {
    run(Post(newCoffee)).mapTo[Unit]
    HttpResponse(204)
  }

  def update(id: Int, newCoffee: Coffee) =
    run(Update(id, newCoffee)).mapTo[Int] map {
      case affectedRowCount: Int if affectedRowCount > 0 =>
        HttpResponse(204)
      case _ =>
        HttpResponse(404)
    }

  def listAll() = run(ListAll).mapTo[Vector[Coffee]]

  def run(cmd: Command) = {
    val query = cmd match {
      case Post(newCoffee) =>
        DBIO.seq(coffees += newCoffee)
      case Get(id) =>
        coffees.filter(_.id === id).result
      case Delete(id) =>
        coffees.filter(_.id === id).delete
      case Update(id, newCoffeeData) =>
        coffees filter (_.id === id) update newCoffeeData.updateId(id)
      case ListAll =>
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

case class Get(id: Int) extends Command

case class Delete(id: Int) extends Command

case class Post(newCoffee: Coffee) extends Command

case class Update(id: Int, newCoffee: Coffee) extends Command

case object ListAll extends Command

case class CommandMsg(cmd: Command, sender: ActorRef)
