package coffeecraft.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorFlowMaterializer
import akka.util.Timeout
import coffeecraft.dao.CoffeeDao
import coffeecraft.models._
import spray.json._

import scala.concurrent.duration._


case class Coisa(ss: String)

trait MyMarshalling extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val coisaFormat = jsonFormat1(Coisa)
  implicit val coffeeFormat = jsonFormat3(Coffee)
}


object CoffeecraftHttpServer extends App with MyMarshalling {

  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorFlowMaterializer()


  implicit val timeout = Timeout(5 seconds) // needed for `?` below

  val route =
    path("coffee" / IntNumber) { coffeeId: Int =>
      get {
        rejectEmptyResponse {
          complete {
            CoffeeDao.get(coffeeId)
          }
        }
      } ~ delete {
        complete {
          CoffeeDao.delete(coffeeId)
        }
      } ~ (put & entity(as[Coffee])) { newCoffee =>
        complete {
          CoffeeDao.update(coffeeId, newCoffee)
        }
      }
    } ~ path("coffee") {
      get {
        complete {
          CoffeeDao.listAll()
        }
      } ~ (post & entity(as[Coffee])) { newCoffee =>
        complete {
          CoffeeDao.post(newCoffee)
        }
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  /*
    def receive = {
      case "Die" =>
        // for the future transformations
        bindingFuture
          .flatMap(_.unbind()) // trigger unbinding from the port
          .onComplete(_ ⇒ system.terminate())
    }
  */
}
