package coffeecraft.dao

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.HttpResponse
import coffeecraft.models._
import spray.json.DefaultJsonProtocol

import scala.concurrent.ExecutionContext.Implicits.global


trait MyMarshallingg extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val coffeeFormat = jsonFormat3(Coffee)
  implicit val invFormat = jsonFormat3(Inventory)
}

case class GenericDaoRestInterface[E <: EntityWithId, T <: TableWithId[E], K](dao: GenericDao[E, T, K]) extends MyMarshallingg {

  def get(id: K) =
    dao.fetchById(id)

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