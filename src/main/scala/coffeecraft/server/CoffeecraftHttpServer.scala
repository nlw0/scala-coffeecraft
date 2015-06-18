package coffeecraft.server

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorFlowMaterializer
import coffeecraft.InitDB
import coffeecraft.dao.{CoffeeDaoRestInterface, InventoryDaoRestInterface}
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
    path("coffee") {
      path(IntNumber) { coffeeId: Int =>
        (get & rejectEmptyResponse) {
          complete {
            CoffeeDaoRestInterface.get(coffeeId)
          }
        } ~
        delete {
          complete {
            CoffeeDaoRestInterface.delete(coffeeId)
          }
        } ~
        (put & entity(as[Coffee])) { newCoffee =>
          complete {
            CoffeeDaoRestInterface.put(coffeeId, newCoffee)
          }
        }
      } ~
      get {
        complete {
          CoffeeDaoRestInterface.listAll()
        }
      } ~
      (post & entity(as[Coffee])) { newCoffee =>
        complete {
          CoffeeDaoRestInterface.post(newCoffee)
        }
      }
    } ~
    path("inv") {
      path(IntNumber) { uid: Int =>
        (get & rejectEmptyResponse) {
          complete {
            InventoryDaoRestInterface.get(uid)
          }
        } ~
        delete {
          complete {
            InventoryDaoRestInterface.delete(uid)
          }
        }
      } ~
      get {
        complete {
          InventoryDaoRestInterface.listAll()
        }
      } ~
      (post & entity(as[Inventory])) { newCoffee =>
        complete {
          InventoryDaoRestInterface.post(newCoffee)
        }
      }
    }


  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

}
