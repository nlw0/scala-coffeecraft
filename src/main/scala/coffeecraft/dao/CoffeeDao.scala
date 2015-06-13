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

  // import system.dispatcher

  val foods = TableQuery[Coffees]

  val db = Database.forConfig("h2mem1")

  val createTable = DBIO.seq(
    foods.schema.create,
    foods += Coffee("Milk", 2.0),
    foods += Coffee("Coffee", 3.25),
    foods += Coffee("Sparkling water", 1.20),
    foods += Coffee("Jameson", 12.50)
  )

  Await.result(db.run(createTable), Duration.Inf)

  def get(id: Int) = run(Get(id)).mapTo[Vector[Coffee]]

  def delete(id: Int) = {
    run(Delete(id)) map {
      case deletedRowCount: Int if deletedRowCount > 0 =>
        HttpResponse(204)
      case _ =>
        HttpResponse(404)
    }
  }

  def run(cmd: Command) = {
    val query = cmd match {
      case Post(newFood) =>
        DBIO.seq(foods += newFood)
      case Get(id) =>
        foods.filter(_.id === id).result
      case Delete(id) =>
        foods.filter(_.id === id).delete
      case ListAll =>
        foods.result
    }
    db.run(query)
  }

  def post(newFood: Coffee) = run(Post(newFood)).mapTo[Unit]

  def listAll() = run(ListAll).mapTo[Vector[Coffee]]
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

case class Post(newFood: Coffee) extends Command

case object ListAll extends Command

case class CommandMsg(cmd: Command, sender: ActorRef)
