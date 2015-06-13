import akka.actor.{ActorRef, Actor, ActorSystem, Props}
import akka.camel.{CamelMessage, Consumer}
import akka.pattern.pipe
import coffeecraft.models.{Food, Foods}
import slick.driver.H2Driver.api._

import scala.concurrent.ExecutionContext.Implicits.global

object Example extends App {
  val system = ActorSystem("my-system")
  system.actorOf(Props[ExampleConsumer])
}

object MessageTranslator {
  def apply(input: String) = input.split(" ").toList match {
    case "POST" :: name :: price :: Nil =>
      Post(name.trim, price.toFloat)
    case "GET" :: name :: Nil =>
      Get(name.trim)
    case "DEL" :: name :: Nil =>
      Delete(name)
    case "LIST" :: Nil =>
      List
  }
}


class ExampleConsumer extends Consumer {
  def endpointUri = "netty:tcp://localhost:60001?textline=true"

  val dbh = context.actorOf(Props[databaseHandler])

  def receive = {
    case msg: CamelMessage =>
      println("received %s" format msg.bodyAs[String])
      dbh ! CommandMsg(MessageTranslator(msg.bodyAs[String]), sender)
  }
}


class databaseHandler extends Actor {

  val foods = TableQuery[Foods]

  val db = Database.forConfig("h2mem1")

  val createTable = DBIO.seq(
    // create the schema
    foods.schema.create,
    // insert two User instances
    foods += Food("Milk", 2.0),
    foods += Food("Coffee", 3.25),
    foods += Food("Sparkling water", 1.20),
    foods += Food("Jameson", 12.50),

    // print the users (select * from USERS)
    foods.result.map(println)
  )

  // try Await.result(db.run(whattorun), Duration.Inf) finally db.close()
  db.run(createTable) onComplete { _ =>
    context.become(normalState)
  }

  def receive = {
    case _ =>
  }

  def normalState: Receive = {
    case CommandMsg(cmd, who) =>
      db.run(queryFromCommand(cmd)) pipeTo who
  }

  def queryFromCommand(cmd: Command) = cmd match {
    case Post(name, price) =>
      DBIO.seq(
        foods += Food(name, price)
      )
    case Get(name)=>
      foods.filter(_.name === name).result
    case Delete(name) =>
      foods.filter(_.name === name).delete
    case List =>
      foods.result
  }
}

trait Command

case class Get(name: String) extends Command

case class Delete(name: String) extends Command

case class Post(name: String, price: Float) extends Command

case object List extends Command

case class CommandMsg(cmd: Command, sender: ActorRef)
