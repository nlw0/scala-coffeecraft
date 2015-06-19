package coffeecraft.server

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorFlowMaterializer
import coffeecraft.InitDB
import coffeecraft.dao.{CoffeeRestInterface, InventoryRestInterface}
import coffeecraft.models._
import spray.json._


trait MyMarshalling extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val coffeeFormat = jsonFormat3(Coffee)
  implicit val invFormat = jsonFormat3(Inventory)
}


object CoffeecraftHttpServer extends App with MyMarshalling {

  InitDB()

  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorFlowMaterializer()

  val route =
    path("coffee" / IntNumber) { coffeeId: Int =>
      (get & rejectEmptyResponse) {
        complete {
          CoffeeRestInterface.get(coffeeId)
        }
      } ~
      delete {
        complete {
          CoffeeRestInterface.delete(coffeeId)
        }
      } ~
      (put & entity(as[Coffee])) { newCoffee =>
        complete {
          CoffeeRestInterface.put(coffeeId, newCoffee)
        }
      }
    } ~
    path("coffee") {
      get {
        complete {
          CoffeeRestInterface.listAll()
        }
      } ~
      (post & entity(as[Coffee])) { newCoffee =>
        complete {
          CoffeeRestInterface.post(newCoffee)
        }
      }
    } ~
    path("inv" / IntNumber) { coffeeId: Int =>
      (get & rejectEmptyResponse) {
        complete {
          InventoryRestInterface.get(coffeeId)
        }
      } ~
      delete {
        complete {
          InventoryRestInterface.delete(coffeeId)
        }
      } ~
      (put & entity(as[Inventory])) { newCoffee =>
        complete {
          InventoryRestInterface.put(coffeeId, newCoffee)
        }
      }
    } ~
    path("inv") {
      get {
        complete {
          InventoryRestInterface.listAll()
        }
      } ~
      (post & entity(as[Inventory])) { newCoffee =>
        complete {
          InventoryRestInterface.post(newCoffee)
        }
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
}
