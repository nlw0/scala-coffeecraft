package coffeecraft.server

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
  implicit val materializer = ActorMaterializer()

  val craftingProcessor = system.actorOf(Props(classOf[CraftingProcessor]), "crafting-processor")

  val userActors = Map(102L -> system.actorOf(Props(classOf[UserInventory], 102L)))

  val optionsSupport = options { ctx => ctx.complete("") }

  def crudRoute[E, T <: Table[E]](nome: String, dao: GenericDaoRestInterface[E, T, Long])(implicit fmt: RootJsonFormat[E]): Route =
    path(nome / LongNumber) { entityId: Long =>
      (get & rejectEmptyResponse) { ctx => ctx.complete(dao.get(entityId)) } ~
      (put & entity(as[E])) { newCoffee => ctx => ctx.complete(dao.put(entityId, newCoffee)) } ~
      delete { ctx => ctx.complete(dao.delete(entityId)) } ~
      optionsSupport
    } ~
    path(nome) {
      get { ctx => ctx.complete(dao.listAll()) } ~
      (post & entity(as[E])) { newCoffee => ctx => ctx.complete(dao.post(newCoffee)) } ~
      optionsSupport
    }

  implicit val askTimeout: Timeout = 5.seconds

  val CoffeecraftUser: PathMatcher1[ActorRef] = LongNumber flatMap { uid => userActors.get(uid) }

  val appRoutes =
    crudRoute[Coffee, Coffees]("coffee", CoffeeRestInterface) ~
    crudRoute[Recipe, Recipes]("recipe", RecipeRestInterface) ~
    crudRoute[Ingredient, Ingredients]("ingredients", IngredientRestInterface) ~
    pathPrefix("user" / CoffeecraftUser) { usr: ActorRef =>
      (pathEnd & get) { ctx =>
          ctx.complete((usr ? ListCmd).mapTo[CoolUserState])
        } ~
      (path("craft") & post & entity(as[List[Long]])) { invIds => ctx =>
          ctx.complete((usr ? CraftCmd(invIds.toSet)) flatMap { x => (usr ? ListCmd).mapTo[CoolUserState] })
        } ~
      (path("sell") & post & entity(as[List[Long]])) { invIds => ctx =>
          ctx.complete((usr ? SellCmd(invIds.toSet)) flatMap { x => (usr ? ListCmd).mapTo[CoolUserState] })
        } ~
      (path("mine") & post) { ctx =>
          ctx.complete((usr ? MineCmd) flatMap { x => (usr ? ListCmd).mapTo[CoolUserState] })
        } ~
      (pathEnd | path("craft") | path("sell") | path("mine")) { optionsSupport }
    }

  val corsHeaders = List(
    RawHeader("Access-Control-Allow-Origin", "*"),
    RawHeader("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS, DELETE"),
    RawHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization")
  )

  val corsRoutes = {
    respondWithHeaders(corsHeaders) {
      appRoutes
    }
  }

  val route = corsRoutes

  val bindingFuture = Http().bindAndHandle(route, "::0", 8080)
}
