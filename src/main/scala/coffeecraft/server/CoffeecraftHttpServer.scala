package coffeecraft.server

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{PathMatcher1, Route}
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import coffeecraft.InitDB
import coffeecraft.dao._
import coffeecraft.domain.{CraftingProcessor, UserInventory}
import UserInventory._
import coffeecraft.models.{_}
import slick.driver.H2Driver.api._
import spray.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps


trait CoffeecraftMarshalling extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val coffeeFmt = jsonFormat3(Coffee)
  implicit val inventoryFmt = jsonFormat3(Inventory)
  implicit val recipeFmt = jsonFormat2(Recipe)
  implicit val ingredientFmt = jsonFormat2(Ingredient)
  implicit val invItemFmt = jsonFormat2(InventoryItem)
  implicit val userStateFmt = jsonFormat2(CoolUserState)
}


object CoffeecraftHttpServer extends App with CoffeecraftMarshalling {

  implicit val askTimeout: Timeout = 5 seconds

  InitDB()

  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()

  val craftingProcessor = system.actorOf(Props(classOf[CraftingProcessor]), "crafting-processor")

  var userActors = Map[Long, ActorRef]()

  val optionsSupport = options { ctx => ctx.complete("") }

  def crudRoute[E, T <: Table[E]](nome: String, dao: GenericDaoRestInterface[E, T, Long])(implicit fmt: RootJsonFormat[E]): Route =
    path(nome / LongNumber) { entityId: Long =>
      (get & rejectEmptyResponse) { ctx => ctx.complete(dao.get(entityId)) } ~
      (put & entity(as[E])) { newCoffee => ctx => ctx.complete(dao.put(entityId, newCoffee)) } ~
      delete { ctx => ctx.complete(dao.delete(entityId)) } ~
      optionsSupport
    } ~ path(nome) {
      get { ctx => ctx.complete(dao.listAll()) } ~
      (post & entity(as[E])) { newCoffee => ctx => ctx.complete(dao.post(newCoffee)) } ~
      optionsSupport
    }

  def uidIsValid(uid: Long) = 100 <= uid && uid < 200

  val CoffeecraftUser: PathMatcher1[ActorRef] = LongNumber flatMap { uid =>
    if (!uidIsValid(uid)) None
    else {
      if (!(userActors contains uid))
        userActors += uid -> system.actorOf(Props(classOf[UserInventory], uid))
      userActors.get(uid)
    }
  }

  val crudRoutes =
    crudRoute[Coffee, Coffees]("coffee", CoffeeRestInterface) ~
    crudRoute[Recipe, Recipes]("recipe", RecipeRestInterface) ~
    crudRoute[Ingredient, Ingredients]("ingredients", IngredientRestInterface)

  val userRoutes = pathPrefix("user" / CoffeecraftUser) { usr: ActorRef =>
    (pathEnd & get) { ctx =>
      ctx.complete((usr ? ListCmd).mapTo[CoolUserState]) } ~
    (path("craft") & post & entity(as[List[Long]])) { invIds => ctx =>
      ctx.complete((usr ? CraftCmd(invIds.toSet)) flatMap { x => (usr ? ListCmd).mapTo[CoolUserState] }) } ~
    (path("sell") & post & entity(as[List[Long]])) { invIds => ctx =>
      ctx.complete((usr ? SellCmd(invIds.toSet)) flatMap { x => (usr ? ListCmd).mapTo[CoolUserState] }) } ~
    (path("mine") & post) { ctx =>
      ctx.complete((usr ? MineCmd) flatMap { x => (usr ? ListCmd).mapTo[CoolUserState] }) } ~
    (pathEnd | path("craft") | path("sell") | path("mine")) { optionsSupport }
  }

  val corsHeaders = List(
    RawHeader("Access-Control-Allow-Origin", "*"),
    RawHeader("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS, DELETE"),
    RawHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization")
  )

  val corsRoutes = respondWithHeaders(corsHeaders) {
    crudRoutes ~ userRoutes
  }

  val route = corsRoutes

  val bindingFuture = Http().bindAndHandle(route, "::0", 8080)
}
