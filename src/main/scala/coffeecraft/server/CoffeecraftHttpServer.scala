package coffeecraft.server

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorFlowMaterializer
import coffeecraft.InitDB
import coffeecraft.dao._
import coffeecraft.models._
import spray.json._


trait MyMarshalling extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val coffeeFmt = jsonFormat3(Coffee)
  implicit val inventoryFmt = jsonFormat3(Inventory)
  implicit val recipeFmt = jsonFormat2(Recipe)
  implicit val ingredientFmt = jsonFormat2(Ingredient)
}


object CoffeecraftHttpServer extends App with MyMarshalling {

  InitDB()

  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorFlowMaterializer()

  def crudRoute[E <: EntityWithId, T <: TableWithId[E]](nome: String, dao: GenericDaoRestInterface[E, T, Long])(implicit fmt: RootJsonFormat[E]): Route =
    path(nome / LongNumber) { entityId: Long =>
      (get & rejectEmptyResponse) { ctx => ctx.complete(dao.get(entityId)) } ~
      (put & entity(as[E])) { newCoffee => ctx => ctx.complete(dao.put(entityId, newCoffee)) } ~
      delete { ctx => ctx.complete(dao.delete(entityId)) }
    } ~
    path(nome) {
      get { ctx => ctx.complete(dao.listAll()) } ~
      (post & entity(as[E])) { newCoffee => ctx => ctx.complete(dao.post(newCoffee)) }
    }

  val route =
    crudRoute[Coffee, Coffees]("coffee", CoffeeRestInterface) ~
    crudRoute[Inventory, Inventories]("inv", InventoryRestInterface) ~
    crudRoute[Recipe, Recipes]("recipe", RecipeRestInterface) ~
    crudRoute[Ingredient, Ingredients]("ingredients", IngredientRestInterface)

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
}
