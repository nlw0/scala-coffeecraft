package coffeecraft.dao

import akka.http.scaladsl.model.HttpResponse
import slick.driver.H2Driver.api._

import scala.concurrent.ExecutionContext.Implicits.global


case class GenericDaoRestInterface[E, T <: Table[E], K](dao: GenericDao[E, T, K]) {

  def get(id: K) =
    dao.fetchOneById(id)

  def delete(id: K) = {
    dao.remove(id) map {
      case affectedRowCount: Int if affectedRowCount > 0 =>
        HttpResponse(204)
      case _ =>
        HttpResponse(404)
    }
  }

  def post(newCoffee: E) = {
    dao.insert(newCoffee)
    HttpResponse(204)
  }

  def put(id: K, newCoffee: E) =
    dao.update(id, newCoffee) map {
      case affectedRowCount: Int if affectedRowCount > 0 =>
        HttpResponse(204)
      case _ =>
        HttpResponse(404)
    }

  def listAll() = dao.fetchAll()
}


object CoffeeRestInterface extends GenericDaoRestInterface(CoffeeDao)

object InventoryRestInterface extends GenericDaoRestInterface(InventoryDao)

object RecipeRestInterface extends GenericDaoRestInterface(RecipeDao)

object IngredientRestInterface extends GenericDaoRestInterface(IngredientDao)