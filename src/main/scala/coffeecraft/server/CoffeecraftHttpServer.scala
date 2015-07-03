package coffeecraft.server

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.ActorFlowMaterializer
import akka.util.Timeout
import coffeecraft.InitDB
import coffeecraft.dao._
import coffeecraft.models.UserInventory._
import coffeecraft.models.{CraftingProcessor, _}
import slick.driver.H2Driver.api._
import spray.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

trait MyMarshalling extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val coffeeFmt = jsonFormat3(Coffee)
  implicit val inventoryFmt = jsonFormat3(Inventory)
  implicit val recipeFmt = jsonFormat2(Recipe)
  implicit val ingredientFmt = jsonFormat2(Ingredient)
  implicit val invItemFmt = jsonFormat2(InventoryItem)
  implicit val userStateFmt = jsonFormat2(CoolUserState)
}


object CoffeecraftHttpServer extends App with MyMarshalling {

  InitDB()

  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorFlowMaterializer()

  val craftingProcessor = system.actorOf(Props(classOf[CraftingProcessor]), "crafting-processor")

  val userActors = Map(102L -> system.actorOf(Props(classOf[UserInventory], 102L)))

  def crudRoute[E, T <: Table[E]](nome: String, dao: GenericDaoRestInterface[E, T, Long])(implicit fmt: RootJsonFormat[E]): Route =
    path(nome / LongNumber) { entityId: Long =>
      (get & rejectEmptyResponse) { ctx => ctx.complete(dao.get(entityId)) } ~
      (put & entity(as[E])) { newCoffee => ctx => ctx.complete(dao.put(entityId, newCoffee)) } ~
      delete { ctx => ctx.complete(dao.delete(entityId)) }
    } ~
    path(nome) {
      get { ctx => ctx.complete(dao.listAll()) } ~
      (post & entity(as[E])) { newCoffee => ctx => ctx.complete(dao.post(newCoffee)) }
    }

  def crudRouteTuple[E, T <: Table[E]](nome: String, dao: GenericDaoRestInterface[E, T, (Long, Long)])(implicit fmt: RootJsonFormat[E]): Route =
    path(nome / LongNumber / LongNumber) { (entityIdA: Long, entityIdB: Long) =>
      val entityId = (entityIdA, entityIdB)
      (get & rejectEmptyResponse) { ctx => ctx.complete(dao.get(entityId)) } ~
      (put & entity(as[E])) { newCoffee => ctx => ctx.complete(dao.put(entityId, newCoffee)) } ~
      delete { ctx => ctx.complete(dao.delete(entityId)) }
    } ~
    path(nome) {
      get { ctx => ctx.complete(dao.listAll()) } ~
      (post & entity(as[E])) { newCoffee => ctx => ctx.complete(dao.post(newCoffee)) }
    }

  implicit val askTimeout: Timeout = 5.seconds

  val appRoutes =
    crudRoute[Coffee, Coffees]("coffee", CoffeeRestInterface) ~
    crudRouteTuple[Inventory, Inventories]("inv", InventoryRestInterface) ~
    path("inv" / LongNumber) { uid: Long =>
      (get & rejectEmptyResponse) { ctx => ctx.complete(InventoryDao.fetchInventory(uid).map(_.toList)) }
    } ~
    crudRoute[Recipe, Recipes]("recipe", RecipeRestInterface) ~
    crudRoute[Ingredient, Ingredients]("ingredients", IngredientRestInterface) ~
    pathPrefix("api" / LongNumber) { uid: Long =>
      val usr = userActors(uid)
      (pathEnd & get) { ctx =>
          ctx.complete((usr ? ListCmd).mapTo[CoolUserState])
        } ~ (path("craft") & post & entity(as[List[Long]])) { invIds => ctx =>
          ctx.complete((usr ? CraftCmd(invIds.toSet)) flatMap { x => (usr ? ListCmd).mapTo[CoolUserState] })
        } ~ (path("sell") & post & entity(as[List[Long]])) { invIds => ctx =>
          ctx.complete((usr ? SellCmd(invIds.toSet)) flatMap { x => (usr ? ListCmd).mapTo[CoolUserState] })
        } ~ (path("mine") & post) { ctx =>
          ctx.complete((usr ? MineCmd) flatMap { x => (usr ? ListCmd).mapTo[CoolUserState] })
        }
    }

  val optionsSupport = {
    options {
      complete("")
    }
  }

  val corsHeaders = List(
    RawHeader("Access-Control-Allow-Origin", "*"),
    RawHeader("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS, DELETE"),
    RawHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization")
  )

  val corsRoutes = {
    respondWithHeaders(corsHeaders) {
      optionsSupport ~ appRoutes
    }
  }

  val route = corsRoutes

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
}
